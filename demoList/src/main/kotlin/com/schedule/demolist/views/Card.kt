package com.schedule.demolist.views

import com.schedule.demolist.*
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color


internal class Card() : VBox(), IView {

        var header = Label("header")
        var deadline = Label("deadline")
        var tagLabel = Label("")
        var checkbox = CheckBox()

        var description = Label("description")

        private var gridExpectedSize = 5
        private val headerConstraint = ColumnConstraints()
        private val dueConstraint = ColumnConstraints()
        private val checkBoxConstraint = ColumnConstraints()
        var tags = HashSet<String>()
        var newTags = ""

        private var displayTags = true

        private val grid = GridPane()

//        private val description = Label("description")

        override fun updateView() { }

        override fun resizeWindowWidth() {
                newTags = ""
                for (i in 0 until tags.size) {
                        if (i != 0) {
                                newTags += ", "
                        }
                        newTags += tags.elementAt(i)
                }
                tagLabel.text = newTags
                if (newTags == "" && displayTags) {
                        gridExpectedSize -= 1
                        grid.children.removeIf { node -> GridPane.getRowIndex(node) === 1 }
                        displayTags = false
                } else if (newTags != "" && !displayTags) {
                        gridExpectedSize += 1
                        grid.add(tagLabel, 0, 1, 2, 1)
                        displayTags = true
                }

//                println("width is ${this.width} with bounding size of ${this.boundsInParent.width} and children size is ${grid.children.size} and grid expected is $gridExpectedSize")
//                if ((this.width <= SCREEN_WIDTH + 50.0) && (grid.children.size == gridExpectedSize)) {
//                        grid.children.removeIf { node -> GridPane.getColumnIndex(node) == 1 }
//                        headerConstraint.percentWidth = 90.0
//                } else if ((this.width > SCREEN_WIDTH + 50.0) && (grid.children.size == gridExpectedSize - 1)) {
//                        headerConstraint.percentWidth = 50.0
//                        grid.add(deadline, 1, 0, 1, 1)
//                }
        }

        override fun resizeWindowHeight() { }

        init {
                grid.padding = Insets(0.0,5.0,0.0,15.0)
                grid.add(header, 0, 0, 1, 1)
                grid.add(deadline, 1, 0, 1, 1)
                grid.add(checkbox, 2, 0, 1, 1)
                grid.add(tagLabel, 0, 1, 2, 1)
                grid.add(description, 0, 2, 2, 1)

                header.style = "-fx-font-size: 20; -fx-font-weight: bold;"
                header.textFill = Color.WHITE
                deadline.style = "-fx-font-size: 20"
                deadline.textFill = Color.WHITE
                checkbox.setMinSize(20.0, 20.0)
                checkbox.isDisable = true

                description.textFill = Color.WHITE
                description.setMinSize(75.0, 20.0)
                description.padding = Insets(10.0, 0.0, 10.0, 10.0)
                tagLabel.textFill = Color.WHITE
                tagLabel.setMinSize(75.0,20.0)
                tagLabel.style = "-fx-font-weight: bold;"
                tagLabel.padding = Insets(10.0, 0.0, 0.0, 10.0)

                this.border = REGULAR_BORDER
                grid.background = Background(BackgroundFill(cardViewColor, null, null))


                children.add(grid)

                headerConstraint.isFillWidth = true
//                headerConstraint.percentWidth = 50.0
                headerConstraint.hgrow = Priority.ALWAYS

//                dueConstraint.isFillWidth = true
                dueConstraint.percentWidth = 35.0
                dueConstraint.hgrow = Priority.ALWAYS

                checkBoxConstraint.maxWidth = checkbox.minWidth
                checkBoxConstraint.halignment = HPos.RIGHT
                checkBoxConstraint.hgrow = Priority.NEVER


                grid.columnConstraints.addAll(headerConstraint, dueConstraint, checkBoxConstraint)
        }
}