package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle


class Login (
    private val model: Model
) : StackPane(), IView {

    var grid = GridPane()
    val back = Rectangle()

    var titleLabel = Label()

    var usernameLabel = Label("Username: ")
    var usernameField = TextField("")

    var passwordLabel = Label("Password: ")
    var passwordField = PasswordField()

    var invalidLabel = Label()

    override fun updateView() {
        if (model.whichLogin == LoginMode.LOGIN) {
            titleLabel.text = "LOGIN"
        } else if (model.whichLogin == LoginMode.REGISTER) {
            titleLabel.text = "REGISTER"
        }

        Platform.runLater {
            if (model.curFocus == Focus.OUTER) {
                this.requestFocus()
            } else if (model.curFocus == Focus.USERNAME) {
                usernameField.requestFocus()
            } else if (model.curFocus == Focus.PASSWORD) {
                passwordField.requestFocus()
            }
        }
    }

    override fun resizeWindowWidth() {
//        println(" width of the node is ${this.width}, the width of the grid is ${grid.width}, the width of background is ${back.width}")
        val windowWidth = this.width
        val gradientWidth = editViewMinGridWidth / editViewMaxGridWidth
        val widthFunc = gradientWidth * windowWidth + editViewMinGridWidth - (SCREEN_WIDTH * gradientWidth)
        grid.minWidth = widthFunc
        grid.maxWidth = widthFunc

        back.x = grid.layoutX - 5
        back.width = grid.minWidth + 10
    }

    override fun resizeWindowHeight() {
//        println("window height is ${this.height}, grid height is ${grid.height}, height of background in ${back.height}")
        val windowHeight = this.height
        val gradientHeight = editViewMinGridHeight / editViewMaxGridHeight
        val heightFunc = gradientHeight*windowHeight + editViewMinGridHeight - (SCREEN_HEIGHT * gradientHeight)
        grid.minHeight = heightFunc
        grid.maxHeight = heightFunc

        back.y = grid.layoutY - 5
        back.height = grid.minHeight + 10
    }

    private fun setPrompt() {
        usernameField.promptText = "Username"
        passwordField.promptText = "Password"
    }

    private fun setProps() {
        back.x = grid.layoutX - 5
        back.y = grid.layoutY - 5
        back.width = grid.width + 10
        back.height = grid.height + 10
        back.arcWidth = 20.0
        back.arcHeight = 20.0
        back.stroke = editViewBorder
        back.fill = Color.TRANSPARENT

        usernameLabel.textFill = editViewTextColor
        passwordLabel.textFill = editViewTextColor
    }

    private fun setGrid() {
        grid.alignment = Pos.TOP_CENTER
        grid.setMaxSize(300.0, 300.0)
        grid.hgap = 10.0
        grid.vgap = 10.0
        grid.border = Border(BorderStroke(editViewColor, BorderStrokeStyle.SOLID, CornerRadii(1.0), BorderWidths(10.0)))
        grid.background = Background(BackgroundFill(editViewColor, null, null))
        grid.padding = Insets(25.0, 25.0, 25.0, 25.0)

        grid.add(titleLabel, 0, 0, 2, 1)

        grid.add(usernameLabel, 0, 1, 1, 1)
        grid.add(usernameField, 0, 2, 1, 1)

        grid.add(passwordLabel, 0, 3, 1, 1)
        grid.add(passwordField, 0, 4, 1, 1)

        grid.add(invalidLabel, 0, 5, 1, 1)

        // need to set a button that allows them to change password or something idk

    }

    init {
        setPrompt()
        setProps()
        setGrid()
        titleLabel.textFill = Color.WHITE
        titleLabel.style = "-fx-font-size: 18; -fx-font-weight: bold;"
        invalidLabel.textFill = Color.RED

        this.children.add(grid)
        this.children.add(back)
        model.addView(this)
    }

    fun loginIsValid(): Boolean {
        var result = false
        model.curFocus = Focus.OUTER

        val user = usernameField.text
        val details = passwordField.text
        when (model.whichLogin) {
            LoginMode.LOGIN -> {
                result = model.dbLayer.attemptLogin(user, details)
            }

            LoginMode.REGISTER -> {
                if (details.length >= 4 && user.length >= 4) {
                    result = model.dbLayer.createNewUser(user, details)
                }
            }
        }
        usernameField.text = ""
        passwordField.text = ""

        if (result) {
            invalidLabel.text = ""
        } else {
            when (model.whichLogin) {
                LoginMode.LOGIN -> invalidLabel.text = "Invalid Login. Please try again."
                LoginMode.REGISTER -> invalidLabel.text = "Invalid Register. Please try again."
            }
        }

        return result
    }
}