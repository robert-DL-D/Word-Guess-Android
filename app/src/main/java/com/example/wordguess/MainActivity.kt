package com.example.wordguess

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

// TODO
// local wifi multiplayer
// word length choice
// word list choice
class MainActivity : AppCompatActivity() {

    private var listOfListOfBoxes = mutableListOf<List<TextView>>()
    private var listOfButtons = mutableListOf<Button>()
    private var wordToGuess: String = ""
    private var currentRowIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rowLayouts = mutableListOf<LinearLayout>()
        val letterBoxesLayout = findViewById<LinearLayout>(R.id.letter_boxes)
        val keyboardLayout = findViewById<LinearLayout>(R.id.keyboard)
        val playAgainButton = findViewById<Button>(R.id.play_again_button)
        val enterButton = findViewById<Button>(R.id.key_enter)
        val deleteButton = findViewById<Button>(R.id.key_delete)
        val correctWord = findViewById<TextView>(R.id.correct_word)

        val words = mutableListOf<String>()
        resources.openRawResource(R.raw.five_letter_words_from_englishstudyonline_org).bufferedReader().forEachLine { line ->
                words.add(line)
            }
        wordToGuess = words.random().uppercase()

        updateMainLayoutBackground()

        for (i in 0 until letterBoxesLayout.childCount) {
            val setBoxes = mutableListOf<TextView>()
            val childLayout = letterBoxesLayout.getChildAt(i) as LinearLayout
            for (j in 0 until childLayout.childCount) {
                setBoxes.add(childLayout.getChildAt(j) as TextView)
            }
            listOfListOfBoxes.add(setBoxes)
        }

        for (i in 0 until keyboardLayout.childCount) {
            rowLayouts.add(keyboardLayout.getChildAt(i) as LinearLayout)
        }
        for (rowLayout in rowLayouts) {
            for (i in 0 until rowLayout.childCount) {
                val button = rowLayout.getChildAt(i) as Button
                listOfButtons.add(button)
                button.setOnClickListener {
                    for (box in listOfListOfBoxes[currentRowIndex]) {
                        if (box.text.isEmpty()) {
                            box.text = button.text.toString()
                            return@setOnClickListener
                        }
                    }
                }
            }
        }

        deleteButton.setOnClickListener {
            val boxes = listOfListOfBoxes[currentRowIndex]
            for (i in boxes.indices.reversed()) {
                val box = boxes[i]
                if (box.text.isNotEmpty()) {
                    box.text = ""
                    break
                }
            }
        }

        enterButton.setOnClickListener {
            val boxes = listOfListOfBoxes[currentRowIndex]
            val guess = StringBuilder()
            for (box in boxes) {
                guess.append(box.text.toString())
            }

            when {
                boxes.any { it.text.isEmpty() } -> return@setOnClickListener
                guess.toString().lowercase() !in words -> {
                    val toast =
                        Toast.makeText(applicationContext, "Not in word list", Toast.LENGTH_SHORT)
                    toast.show()
                    toast.view?.postDelayed({ toast.cancel() }, 3000)
                }

                guess.toString() == wordToGuess -> {
                    for (i in guess.indices) {
                        val letterButton =
                            listOfButtons.find { it.text.toString() == guess[i].toString() }
                        setBoxAndButtonBackgroundColor(boxes[i],
                            letterButton,
                            R.drawable.green_box_background,
                            R.color.green_button_background)
                    }
                    playAgainButton.isVisible = true
                    enterButton.isEnabled = false
                    deleteButton.isEnabled = false
                }

                else -> {
                    val highlightedLetters = mutableSetOf<Char>()
                    val correctLetters = mutableSetOf<Int>()
                    val incorrectLetters = mutableSetOf<Int>()
                    var i = 0

                    while (i < guess.length && i < wordToGuess.length) {
                        val letter = guess[i]
                        val letterButton =
                            listOfButtons.find { it.text.toString() == letter.toString() }
                        var boxBackgroundRes = R.drawable.grey_box_background
                        var buttonColorRes = R.color.grey_button_background

                        if (letter == wordToGuess[i]) {
                            boxBackgroundRes = R.drawable.green_box_background
                            buttonColorRes = R.color.green_button_background
                            highlightedLetters.add(letter)
                            correctLetters.add(i)
                        } else if (wordToGuess.contains(letter)) {
                            val indexes =
                                wordToGuess.indices.filterIndexed { index, _ -> index !in correctLetters && index !in incorrectLetters }
                            val correctIndex =
                                indexes.firstOrNull { index -> guess[index] != wordToGuess[index] && wordToGuess[index] == letter }
                            val incorrectIndex =
                                indexes.firstOrNull { index -> wordToGuess[index] == letter }

                            if (correctIndex != null) {
                                boxBackgroundRes = R.drawable.yellow_box_background
                                buttonColorRes = R.color.yellow_button_background
                                highlightedLetters.add(letter)
                                correctLetters.add(correctIndex)
                            } else if (incorrectIndex != null) {
                                boxBackgroundRes = R.drawable.grey_box_background
                                buttonColorRes = R.color.grey_button_background
                                highlightedLetters.add(letter)
                                incorrectLetters.add(incorrectIndex)
                            }
                        }

                        if (letterButton?.backgroundTintList?.defaultColor == ContextCompat.getColor(
                                this,
                                R.color.yellow_button_background) && buttonColorRes != R.color.green_button_background) {
                            buttonColorRes = R.color.yellow_button_background
                        } else if (letterButton?.backgroundTintList?.defaultColor == ContextCompat.getColor(
                                this,
                                R.color.green_button_background)) {
                            buttonColorRes = R.color.green_button_background
                        }

                        setBoxAndButtonBackgroundColor(boxes[i],
                            letterButton,
                            boxBackgroundRes,
                            buttonColorRes)
                        i++
                    }

                    currentRowIndex++

                    if (currentRowIndex == listOfListOfBoxes.size && guess.toString() != wordToGuess) {
                        playAgainButton.visibility = View.VISIBLE
                        enterButton.isEnabled = false
                        deleteButton.isEnabled = false

                        correctWord.text = "The correct word was: $wordToGuess"
                        correctWord.visibility = View.VISIBLE
                    }
                }
            }
        }

        playAgainButton.setOnClickListener {
            enterButton.isEnabled = true
            deleteButton.isEnabled = true
            playAgainButton.visibility = View.INVISIBLE
            correctWord.visibility = View.INVISIBLE
            currentRowIndex = 0
            wordToGuess = words.random().uppercase()

            for (listOfBoxes in listOfListOfBoxes) {
                for (box in listOfBoxes) {
                    box.text = ""
                    box.setBackgroundResource(R.drawable.white_box_background)
                }
            }

            val typedValue = TypedValue()
            this.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            for (button in listOfButtons) {
                button.backgroundTintList = ColorStateList.valueOf(typedValue.data)
            }

        }
    }

    private fun updateMainLayoutBackground() {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main_layout)
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this,
                androidx.cardview.R.color.cardview_dark_background))
        } else {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this,
                androidx.cardview.R.color.cardview_light_background))
        }
    }

    private fun setBoxAndButtonBackgroundColor(
            currentBox: TextView,
            letterButton: Button?,
            boxBackground: Int,
            buttonBackground: Int) {
        currentBox.setBackgroundResource(boxBackground)
        letterButton?.backgroundTintList = ContextCompat.getColorStateList(this, buttonBackground)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        updateMainLayoutBackground()
    }

}
