package com.example.wordguess

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var listOfArrayOfBoxes = mutableListOf<List<TextView>>()
    private var listOfLetterButtons = mutableListOf<Button>()
    private var wordToGuess: String = ""

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
        var setIndex = 0

        val words = mutableListOf<String>()
        resources.openRawResource(R.raw.five_letter_words_from_englishstudyonline_org)
            .bufferedReader().forEachLine { line ->
                words.add(line)
            }
        wordToGuess = words.random().uppercase()

        // FOR TEST
        /*val words = arrayOf("legal", "rally", "learn", "aioli")
        var wordToGuess = "LEARN"*/

        for (i in 0 until letterBoxesLayout.childCount) {
            val setBoxes = mutableListOf<TextView>()
            val childLayout = letterBoxesLayout.getChildAt(i) as LinearLayout
            for (j in 0 until childLayout.childCount) {
                setBoxes.add(childLayout.getChildAt(j) as TextView)
            }
            listOfArrayOfBoxes.add(setBoxes)
        }
        /*letterBoxesLayout.forEach { childLayout ->
            val setBoxes = mutableListOf<TextView>()
            childLayout.forEach { setBoxes.add(it as TextView) }
            listOfArrayOfBoxes.add(setBoxes)
        }*/

        for (i in 0 until keyboardLayout.childCount) {
            rowLayouts.add(keyboardLayout.getChildAt(i) as LinearLayout)
        }
        for (rowLayout in rowLayouts) {
            for (i in 0 until rowLayout.childCount) {
                val button = rowLayout.getChildAt(i) as Button
                listOfLetterButtons.add(button)
                button.setOnClickListener {
                    for (box in listOfArrayOfBoxes[setIndex]) {
                        if (box.text.isEmpty()) {
                            box.text = button.text.toString()
                            return@setOnClickListener
                        }
                    }
                }
            }
        }
        val defaultButtonBackground = listOfLetterButtons.first().backgroundTintList

        deleteButton.setOnClickListener {
            val boxes = listOfArrayOfBoxes[setIndex]
            for (i in boxes.indices.reversed()) {
                val box = boxes[i]
                if (box.text.isNotEmpty()) {
                    box.text = ""
                    break
                }
            }
        }

        enterButton.setOnClickListener {
            val boxes = listOfArrayOfBoxes[setIndex]
            val guess = StringBuilder()

            if (boxes.any { it.text.isEmpty() }) {
                return@setOnClickListener
            }

            for (box in boxes) {
                guess.append(box.text.toString())
            }

            if (!words.contains(guess.toString().lowercase())) {
                val toast =
                    Toast.makeText(applicationContext, "Not in word list", Toast.LENGTH_SHORT)
                toast.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    toast.cancel()
                }, 3000)

            } else {
                if (guess.toString() == wordToGuess) {
                    for (i in guess.indices) {
                        val letterButton =
                            listOfLetterButtons.find { it.text.toString() == guess[i].toString() }

                        setBoxAndButtonBackgroundColor(
                            boxes[i],
                            letterButton,
                            R.drawable.green_box_background,
                            R.color.green_button_background
                        )
                    }

                    playAgainButton.visibility = View.VISIBLE
                    enterButton.isEnabled = false
                    deleteButton.isEnabled = false

                } else {
                    val highlightedLetters = mutableSetOf<Char>()
                    val correctLetters = mutableSetOf<Int>()
                    val incorrectLetters = mutableSetOf<Int>()
                    var i = 0

                    while (i < guess.length && i < wordToGuess.length) {
                        val letter = guess[i]
                        val letterButton =
                            listOfLetterButtons.find { it.text.toString() == letter.toString() }
                        var boxBackgroundRes = R.drawable.grey_box_background
                        var buttonColorRes = R.color.grey_button_background

                        if (letter == wordToGuess[i]) {
                            boxBackgroundRes = R.drawable.green_box_background
                            buttonColorRes = R.color.green_button_background
                            highlightedLetters.add(letter)
                            correctLetters.add(i)
                        } else if (wordToGuess.contains(letter)) {
                            val indexes =
                                wordToGuess.indices.filter { j -> wordToGuess[j] == letter }
                            val correctIndex =
                                indexes.firstOrNull { j -> j !in correctLetters && j !in incorrectLetters && guess[j] != wordToGuess[j] }
                            val incorrectIndex =
                                indexes.firstOrNull { j -> j !in correctLetters && j !in incorrectLetters }

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
                                R.color.yellow_button_background
                            ) && buttonColorRes != R.color.green_button_background
                        ) {
                            buttonColorRes = R.color.yellow_button_background
                        } else if (letterButton?.backgroundTintList?.defaultColor == ContextCompat.getColor(
                                this,
                                R.color.green_button_background
                            )
                        ) {
                            buttonColorRes = R.color.green_button_background
                        }

                        setBoxAndButtonBackgroundColor(
                            boxes[i],
                            letterButton,
                            boxBackgroundRes,
                            buttonColorRes
                        )
                        i++
                    }

                    setIndex++

                    if (setIndex == listOfArrayOfBoxes.size && guess.toString() != wordToGuess) {
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
            setIndex = 0
            wordToGuess = words.random().uppercase()

            for (b in listOfArrayOfBoxes) {
                for (box in b) {
                    box.text = ""
                    box.setBackgroundResource(R.drawable.white_box_background)
                }
            }

            for (button in listOfLetterButtons) {
                button.backgroundTintList = defaultButtonBackground
            }
        }
    }

    private fun setBoxAndButtonBackgroundColor(
        currentBox: TextView,
        letterButton: Button?,
        boxBackground: Int,
        buttonBackground: Int
    ) {
        currentBox.setBackgroundResource(boxBackground)
        letterButton?.backgroundTintList = ContextCompat.getColorStateList(this, buttonBackground)
    }

}