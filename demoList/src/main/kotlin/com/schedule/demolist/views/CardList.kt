package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import kotlin.math.max
import kotlin.math.pow

internal class CardList(
    private val model: Model
) : VBox(), IView {

    private val cardBox = VBox()
    private val scrollPane = ScrollPane()
    // When notified by the model that things have changed,
    // update to display the new value
    private fun moveSelect() {
        var newCard = cardBox.children[model.getIdx()]
        if (newCard is Card) {
            newCard.border = SELECT_BORDER
        }
        cardBox.children[model.getIdx()] = newCard
    }
    private fun populateCards() {
        cardBox.children.clear()
        model.getTasks().forEach{ t ->
            run {
                var card = Card()
                cardBox.children.add(card)
                card.header.text = t.header
                card.description.text = t.summary
                card.deadline.text = t.endDate
                card.checkbox.isSelected = t.done
                card.tags = t.tags
            }
        }
    }

    override fun updateView() {
        if (model.curMode == Mode.VIEWING) {
            populateCards()
            if (model.getIdx() < cardBox.children.size && model.getIdx() >= 0) {
                moveSelect()
            }
//            println("scrollPane size is ${scrollPane.height} while cardlist height is ${this.height} and vBox height is ${cardBox.height} vv values is ${scrollPane.vmax}")
//            println("curr idx is ${model.state.curIdx} while max is ${cardBox.children.size - 1}")
            if (model.getIdx() >= cardBox.children.size - 1) {
                scrollPane.applyCss()
                scrollPane.layout()
                scrollPane.vvalue = scrollPane.vmax
            } else {
                scrollPane.vvalue = (model.getIdx().toDouble() / cardBox.children.size.toDouble())
            }
            cardBox.children.forEach{ card ->
                if (card is Card) card.resizeWindowWidth()
            }
        }
    }

    override fun resizeWindowWidth() {
        val windowWidth = this.boundsInParent.width
        val padding = max(5.0, 0.01*windowWidth.pow(1.3))
        this.padding = Insets(10.0, padding, 10.0, padding)
        cardBox.children.forEach{ card ->
            if (card is Card) card.resizeWindowWidth()
        }
    }

    override fun resizeWindowHeight() { }


    init {
        model.addView(this)
        // add label widget to the pane
        this.alignment = Pos.CENTER
        cardBox.spacing = 5.0
        scrollPane.content = cardBox
        scrollPane.isFitToWidth = true
         cardBox.background = Background(BackgroundFill(mainBackGroundColor, null, null))

        children.add(scrollPane)
        scrollPane.vbarPolicy=ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.style = "-fx-background-color:transparent;"
        this.background = Background(BackgroundFill(mainBackGroundColor, null, null))
        // register with the model when we're ready to start receiving data
    }
}