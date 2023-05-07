package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.geometry.Pos
import javafx.scene.effect.ColorAdjust
import javafx.scene.effect.GaussianBlur
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*

internal class ParentView(
    private val model: Model
) : StackPane(), IView {

    var edit = EditView(model)
    var view = ViewView(model)
    var shitter = back(model)
    var tag = TagView(model)
    var login = Login(model)
    var search = SearchView(model)
    var tagList = TagListView(model)
    var changeData = ChangeDataView(model)

    override fun updateView() { }

    override fun resizeWindowWidth() { }

    override fun resizeWindowHeight() { }

    fun keyReleased(letter: KeyCode) {
        model.keyReleased(letter)
    }

    fun keyDispatch(letter: KeyCode) {
        if (model.curMode == Mode.LOGIN) {
            if (letter == KeyCode.ENTER) {
                if (login.loginIsValid()) {
                    var loadedState = model.dbLayer.getStateFromPersistenceLayer()
                    model.loadState(loadedState!!)
                    view.isVisible = true
                    login.isVisible = false
                } else {
                    return
                }
            }
        } else if (model.curMode == Mode.VIEWING){
            if (model.inputBuffer == "" && letter == KeyCode.E && model.getTasks().size > 0){
                shitter.isVisible = true
                edit.isVisible = true
            } else if ((model.inputBuffer == "T" && (letter == KeyCode.C || letter == KeyCode.D)) ||
                model.inputBuffer == "F" && letter == KeyCode.T) {
//                println("Hit2")
                tag.updateView()
                shitter.isVisible = true
                tag.isVisible = true
            } else if (model.inputBuffer == "F" && (letter == KeyCode.V || letter == KeyCode.N ||
                        letter == KeyCode.D || letter == KeyCode.P)) {
                search.updateView()
                shitter.isVisible = true
                search.isVisible = true
            } else if (model.inputBuffer == "T" && letter == KeyCode.L) {
                tagList.updateView()
                shitter.isVisible = true
                tagList.isVisible = true
            } else if (model.inputBuffer == "" && (letter == KeyCode.P || letter == KeyCode.U)) {
                changeData.updateView()
                shitter.isVisible = true
                changeData.isVisible = true
            }
        } else if (model.curMode == Mode.EDIT){
//            println(model.curFocus)
            if (letter == KeyCode.ESCAPE && model.curFocus == Focus.OUTER){
                shitter.isVisible = false
                edit.isVisible = false
            }
        } else if (model.curMode == Mode.TAG) {
            if (letter == KeyCode.ESCAPE || letter == KeyCode.ENTER){
                tag.isVisible = false
                shitter.isVisible = false
            }
        } else if (model.curMode == Mode.SEARCH) {
            if (letter == KeyCode.ESCAPE || letter == KeyCode.ENTER) {
                search.isVisible = false
                shitter.isVisible = false
            }
        } else if (model.curMode == Mode.TAGLIST) {
            if (letter == KeyCode.ESCAPE || letter == KeyCode.ENTER) {
                tagList.isVisible = false
                shitter.isVisible = false
            }
        } else if (model.curMode == Mode.CHANGE_DATA) {
            if (letter == KeyCode.ESCAPE) {
                changeData.isVisible = false
                shitter.isVisible = false
            }
        }
        model.keyDispatch(letter)
    }

    init {
        this.addEventFilter(MouseEvent.ANY, MouseEvent::consume)
        edit.isVisible = false
        shitter.isVisible = false
        tag.isVisible = false
        view.isVisible = false
        search.isVisible = false
        tagList.isVisible = false
        changeData.isVisible = false
        val adj = ColorAdjust(100.0, 0.0, -100.5, 0.0)
        val blur = GaussianBlur(0.0)
        adj.input = blur
        shitter.effect = adj
        edit.alignment = Pos.CENTER
        tag.alignment = Pos.CENTER
        login.alignment = Pos.CENTER
        this.children.add(login)
        this.children.add(view)
        this.children.add(shitter)
        this.children.add(edit)
        this.children.add(tag)
        this.children.add(search)
        this.children.add(tagList)
        this.children.add(changeData)
        this.background = Background(BackgroundFill(mainBackGroundColor, null, null))
    }
}