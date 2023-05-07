package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

internal class TagView(
    private val model: Model
) : StackPane(), IView {

    private var grid = GridPane()
    private val back = Rectangle()
    private var tagType: Color = Color.BLUE


    private var tagFieldLabel = Label()
    private var tagField = TextField("")

    var curTrieCandidates = ArrayList<String>()
    var curTrieCandidate = 0
    override fun updateView() {
        if (model.whichTag == TagMode.DELETE) {
            tagFieldLabel.text = "Delete Tag:"
            tagType = Color.RED
        } else if (model.whichTag == TagMode.ADD) {
            tagFieldLabel.text = "Create Tag:"
            tagType = Color.GREEN
        } else if (model.whichTag == TagMode.FILTER) {
            tagFieldLabel.text = "Filter By Tag:"
            tagType = Color.WHITE
        }
        tagFieldLabel.textFill = tagType
        back.stroke = tagType

        this.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                val realTag = tagField.text.replace(" ", "-")
                when (model.whichTag) {
                    TagMode.DELETE -> {
                        model.deleteTag(realTag)
                    }

                    TagMode.FILTER -> {
                        model.filterTasks(FilterBy.TAG, realTag)
                    }

                    TagMode.ADD -> {
                        model.addTag(realTag)
                    }
                }

            }
            tagField.text = ""
        }

        Platform.runLater {
            tagField.requestFocus()
        }
    }

    override fun resizeWindowWidth() {
        grid.minWidth = tagFieldLabel.width + tagField.width + 80.0
        grid.maxWidth = tagFieldLabel.width + tagField.width + 80.0
        back.x = grid.layoutX
        back.width = grid.minWidth
    }

    override fun resizeWindowHeight() {
        grid.maxHeight = tagFieldLabel.height + 30
        grid.minHeight = tagFieldLabel.height + 30
        back.y = grid.layoutY
        back.height = grid.minHeight
    }

    private fun setProps() {
        back.x = grid.layoutX - 5
        back.y = grid.layoutY - 5
        back.width = grid.width + 10
        back.height = grid.height + 10
        back.arcWidth = 5.0
        back.arcHeight = 5.0
        back.stroke = tagType
        back.strokeWidth = 2.0
        back.fill = Color.TRANSPARENT
    }

    fun autoComplete(e: KeyEvent){
        var letter = e.code
        if (letter == KeyCode.TAB){
            e.consume()
            var curText = tagField.characters
            var curCheck = curText.length -1
            curTrieCandidate = (curTrieCandidate + 1)%curTrieCandidates.size
            while (curCheck >= 0) {
                if (curText[curCheck] == ',') {
                    break
                }
                curCheck -= 1
            }
            if (curCheck == -1){tagField.text =curTrieCandidates[curTrieCandidate]}
            else{
                var prefix = curText.substring(0, curCheck+1).toString()
                tagField.text = prefix + curTrieCandidates[curTrieCandidate]
            }
            tagField.positionCaret(tagField.text.length)
        }
    }
    fun getAutofillCandidates(letter: KeyCode){
//        println("press")
        if (letter == KeyCode.TAB){return}
        var curText = tagField.characters
        var curCheck = curText.length -1
        while (curCheck >= 0) {
            if (curText[curCheck] == ',') {
                curCheck += 1
                break
            }
            curCheck -= 1
        }
        var partialTag = curText
        if (curCheck != -1){partialTag = curText.substring(curCheck)}
        curTrieCandidates = model.tagTrie.getCandidates(partialTag.toString())
        curTrieCandidates.add(partialTag.toString())
        curTrieCandidate = curTrieCandidates.size -1
    }
    init {
        grid.alignment = Pos.CENTER
        setProps()
        grid.hgap = 10.0
        grid.vgap = 10.0

        tagFieldLabel.style = "-fx-font-weight: bold;"
        grid.add(tagFieldLabel, 0, 0, 2, 1)
        grid.add(tagField, 2, 0, 2, 1)
        grid.padding = Insets(25.0, 25.0, 25.0, 25.0)
        grid.setMaxSize(300.0, 130.0)
        grid.background = Background(BackgroundFill(Color.BLACK, null, null))
        tagField.setOnKeyReleased {e-> getAutofillCandidates(e.code) }
        tagField.addEventFilter(KeyEvent.KEY_PRESSED, {e->autoComplete(e)})


        this.children.add(grid)
        this.children.add(back)

        model.addView(this)
    }
}