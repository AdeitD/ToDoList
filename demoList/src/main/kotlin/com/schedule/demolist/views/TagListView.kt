package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment

internal class TagListView(
    private val model: Model
) : StackPane(), IView {

    private var grid = GridPane()

    private var listLabel = Label()
    private var tagTitle = Label()

    override fun updateView() {
        var curList: HashSet<String> = model.getTagsList();
        var bullet: String = "\u2022"
        var output: String = ""
        curList.forEach{ tag->
            output += ("$bullet$tag  ")
        }
        listLabel.text = output
    }

    override fun resizeWindowWidth() {
        val windowWidth = this.width
        listLabel.minWidth = (windowWidth / 2) + 40.0
        listLabel.maxWidth = (windowWidth / 2) + 40.0
        tagTitle.minWidth = (windowWidth / 2) + 40.0
        tagTitle.maxWidth = (windowWidth / 2) + 40.0
        grid.minWidth = listLabel.minWidth + 40.0
        grid.maxWidth = listLabel.maxWidth + 40.0
        resizeWindowHeight()
    }

    override fun resizeWindowHeight() {
        grid.maxHeight = tagTitle.maxHeight + listLabel.maxHeight + 10
    }

    private fun setProps() {
        grid.alignment = Pos.CENTER

        grid.hgap = 10.0

        tagTitle.text = "Tags"
        tagTitle.alignment = Pos.CENTER
        tagTitle.minWidth = SCREEN_WIDTH / 2
        tagTitle.padding = Insets(0.0, 0.0, 10.0, 0.0)
        tagTitle.textFill = Color.WHITE
        tagTitle.style = "-fx-font-size: 20; -fx-font-weight: bold; -fx-background-color: black; -fx-border-width: 1 1 0 1; -fx-border-color: white"

        listLabel.minWidth = SCREEN_WIDTH / 2
        listLabel.textFill = Color.WHITE
        listLabel.alignment = Pos.CENTER
        listLabel.textAlignment = TextAlignment.CENTER
        listLabel.padding = Insets(0.0, 5.0, 0.0, 5.0)
        listLabel.style = "-fx-font-weight: bold; -fx-background-color: black; -fx-border-width: 0 1 1 1; -fx-border-color: white"
        listLabel.isWrapText = true

        grid.add(tagTitle, 0, 0, 1, 1)
        grid.add(listLabel, 0, 1, 1, 1)
        grid.padding = Insets(0.0, 5.0, 0.0, 5.0)
        grid.setMinSize(SCREEN_WIDTH, SCREEN_HEIGHT / 3)
        grid.background = Background(BackgroundFill(Color.TRANSPARENT, null, null))
    }

    init {
        setProps()
        this.children.add(grid)
        model.addView(this)
    }
}