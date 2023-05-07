package com.schedule.demolist.views

import com.schedule.demolist.IView
import com.schedule.demolist.model.Model
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color

internal class back (
    private val model: Model
) : StackPane(), IView {
    override fun resizeWindowWidth() { }

    override fun resizeWindowHeight() { }

    override fun updateView() { }

    init {
        this.background = Background(BackgroundFill(Color.web("0x39393a"), CornerRadii(0.0), Insets(0.0, 0.0, 0.0, 0.0)))

        this.opacity = 0.8
        model.addView(this)
    }
}