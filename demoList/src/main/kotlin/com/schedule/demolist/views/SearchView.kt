package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

internal class SearchView(
    private val model: Model
) : StackPane(), IView {

    private var grid = GridPane()
    private val back = Rectangle()


    private var valueFieldLabel = Label()
    private var valueField = TextField("")
    private var searchType = Color.GREEN

    override fun updateView() {
        if (model.whichSearch == FilterBy.VALUE) {
            valueFieldLabel.text = "Search:"
            searchType = Color.RED
        } else if (model.whichSearch == FilterBy.NAME) {
            valueFieldLabel.text = "Filter Name:"
            searchType = Color.GREEN
        } else if (model.whichSearch == FilterBy.END_DATE) {
            valueFieldLabel.text = "Filter Date:"
            searchType = Color.WHITE
        } else if (model.whichSearch == FilterBy.PRIORITY) {
            valueFieldLabel.text = "Filter Priority:"
            searchType = Color.BLUE
        }
        back.stroke = searchType
        valueFieldLabel.textFill = searchType

        this.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                model.filterTasks(model.whichSearch, valueField.text)
            }
            valueField.text = ""
        }

        Platform.runLater {
            valueField.requestFocus()
        }
    }

    override fun resizeWindowWidth() {
        grid.minWidth = valueFieldLabel.width + valueField.width + 80.0
        grid.maxWidth = valueFieldLabel.width + valueField.width + 80.0
        back.x = grid.layoutX
        back.width = grid.minWidth
    }

    override fun resizeWindowHeight() {
        grid.maxHeight = valueFieldLabel.height + 30
        grid.minHeight = valueFieldLabel.height + 30
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
        back.stroke = searchType
        back.strokeWidth = 2.0
        back.fill = Color.TRANSPARENT
    }

    init {
        grid.alignment = Pos.CENTER
        setProps()
        grid.hgap = 10.0
        grid.vgap = 10.0

        valueFieldLabel.style = "-fx-font-weight: bold;"
        grid.add(valueFieldLabel, 0, 0, 2, 1)
        grid.add(valueField, 2, 0, 2, 1)
        grid.padding = Insets(25.0, 25.0, 25.0, 25.0)
        grid.setMaxSize(300.0, 130.0)
        grid.background = Background(BackgroundFill(Color.BLACK, null, null))


        this.children.add(grid)
        this.children.add(back)

        model.addView(this)
    }
}