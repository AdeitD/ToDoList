package com.schedule.demolist.views

import com.schedule.demolist.IView
import com.schedule.demolist.model.Model
import javafx.geometry.Pos
import javafx.scene.layout.VBox

internal class ViewView(
    private val model: Model
) : VBox(), IView {
    private val buttonBar = ButtonBarView(model)
    private val tasks = CardList(model)

    override fun resizeWindowWidth() { }

    override fun resizeWindowHeight() { }

    override fun updateView() { }

    init {
        buttonBar.alignment = Pos.TOP_CENTER
//        tasks.alignment = Pos.TOP_CENTER
        this.children.add(buttonBar)
        this.children.add(tasks)
        model.addView(this)
    }
}