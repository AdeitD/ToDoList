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
 internal class ChangeDataView(
    private val model: Model
) : StackPane(), IView {

    private var grid = GridPane()
    private val back = Rectangle()

    private var changeFieldLabel = Label()
    private var changeField = TextField("")

    var statusLabel = Label()

    override fun updateView() {
        if (model.whichChangeData == ChangeMode.CHANGE_USER) {
            changeFieldLabel.text = "New Username:"
        } else if (model.whichChangeData == ChangeMode.CHANGE_DETAILS) {
            changeFieldLabel.text = "New Password:"
        }
        changeFieldLabel.textFill = Color.WHITE
        back.stroke = Color.WHITE

        this.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                var valid = false
                var providedData = changeField.text
                if (model.whichChangeData == ChangeMode.CHANGE_USER) {
                    valid = providedData.length >= 4 && model.dbLayer.updateUserUsername(providedData)
                } else if (model.whichChangeData == ChangeMode.CHANGE_DETAILS) {
                    valid = providedData.length >= 4 && model.dbLayer.updateUserPassword(providedData)
                }

                if (!valid) {
                    statusLabel.textFill = Color.RED
                    statusLabel.text = "Invalid Entry. Try again."
                } else {
                    statusLabel.textFill = Color.GREEN
                    statusLabel.text = "Successfully updated."
                }
            } else if (event.code == KeyCode.ESCAPE) {
                statusLabel.text = ""
            }
            changeField.text = ""
        }

        Platform.runLater {
            changeField.requestFocus()
        }
    }

    override fun resizeWindowWidth() {
        grid.minWidth = changeFieldLabel.width + changeField.width + 80.0
        grid.maxWidth = changeFieldLabel.width + changeField.width + 80.0
        back.x = grid.layoutX
        back.width = grid.minWidth
    }

    override fun resizeWindowHeight() {
        grid.maxHeight = changeFieldLabel.height + statusLabel.height + 80
        grid.minHeight = changeFieldLabel.height + statusLabel.height + 80
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
        back.stroke = Color.WHITE
        back.strokeWidth = 2.0
        back.fill = Color.TRANSPARENT
    }

    init {
        grid.alignment = Pos.CENTER
        setProps()
        grid.hgap = 10.0
        grid.vgap = 10.0

        changeFieldLabel.style = "-fx-font-weight: bold;"

        grid.add(changeFieldLabel, 0, 0, 2, 1)
        grid.add(changeField, 2, 0, 2, 1)

        grid.add(statusLabel, 2, 1, 2, 1)

        grid.padding = Insets(25.0, 25.0, 25.0, 25.0)
        grid.setMaxSize(300.0, 225.0)
        grid.background = Background(BackgroundFill(Color.BLACK, null, null))

        this.children.add(grid)
        this.children.add(back)

        model.addView(this)
    }
}