package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.lang.Double.max
import kotlin.math.pow

internal class ButtonBarView(
    private val model: Model
) : VBox(), IView {

    // good idea to start button off in unknown state
    private val filterLabel = Label("Filters: ")
    private val filtersListLabel = Label("None")
    private val sortLabel = Label("Sort: ")
    private val sortListLabel = Label("None")
    private val grid = GridPane()

    private var filters = HashMap<FilterBy, ArrayList<String>>()

    private fun refreshFilters() {
        filters.clear()
        val appliedFilters = model.getAppliedFilters()
        for ((fb, str) in appliedFilters) {
            if (!filters.containsKey(fb)) { filters[fb] = ArrayList() }
            filters[fb]?.add(str)
        }
    }

    private fun refreshSort() {
        var sorting: SortBy = model.getSort()
        when (sorting) {
            SortBy.ID -> sortListLabel.text = "None"
            SortBy.END_DATE -> sortListLabel.text = "Due Date"
            SortBy.NAME -> sortListLabel.text = "Name"
            SortBy.PRIORITY -> sortListLabel.text = "Priority"
            SortBy.CUSTOM -> sortListLabel.text = "Custom"
        }
    }

    // When notified by the model that things have changed,
    // update to display the new value
    override fun updateView() {
//         just set the button name to the counter
        refreshFilters()
        refreshSort()

        var newFilters = ""
        if (filters.isEmpty()) {
            newFilters = "None, "
        } else {
            for ((key, value) in filters) {
                newFilters += "$key: ${value.joinToString(prefix = "[", postfix = "]", separator = ", ") }"
                newFilters += ", "
            }
        }
        // substring removes the last comma and space
        filtersListLabel.text = newFilters.substring(0, newFilters.length-2)
    }

    override fun resizeWindowWidth() {
        val windowWidth = this.boundsInParent.width
        val padding = max(5.0, 0.01 * windowWidth.pow(1.3))
        this.padding = Insets(15.0, padding, 5.0, padding)
    }

    override fun resizeWindowHeight() { }

    private fun setProps() {
        filterLabel.padding = Insets(10.0, 10.0, 0.0, 10.0)
        filterLabel.textFill = Color.WHITE
        filterLabel.style = "-fx-font-size: 18; -fx-font-weight: bold;"

        filtersListLabel.padding = Insets(10.0, 10.0, 0.0, 10.0)
        filtersListLabel.textFill = Color.WHITE
        filtersListLabel.style = "-fx-font-size: 18;"

        sortLabel.padding = Insets(5.0, 10.0, 5.0, 10.0)
        sortLabel.textFill = Color.WHITE
        sortLabel.style = "-fx-font-size: 18; -fx-font-weight: bold;"

        sortListLabel.padding = Insets(5.0, 10.0, 5.0, 10.0)
        sortListLabel.textFill = Color.WHITE
        sortListLabel.style = "-fx-font-size: 18;"
    }

    init {
        // setup the view (i.e. group+widget)
        this.alignment = Pos.CENTER_LEFT
        this.minHeight = 50.0

//        filterLabel.setMinSize(SCREEN_WIDTH, SCREEN_HEIGHT

        setProps()

        grid.add(filterLabel, 0, 0, 1, 1)
        grid.add(filtersListLabel, 1, 0, 1, 1)

        grid.add(sortLabel, 0, 1, 1, 1)
        grid.add(sortListLabel, 1, 1, 1, 1)


        val filterTitleConstraint = ColumnConstraints()
        filterTitleConstraint.maxWidth = filterLabel.minWidth
        filterTitleConstraint.halignment = HPos.RIGHT
        filterTitleConstraint.hgrow = Priority.NEVER
        val filtersListConstraint = ColumnConstraints()
        filtersListConstraint.isFillWidth = true
        filtersListConstraint.hgrow = Priority.ALWAYS
        grid.columnConstraints.addAll(filterTitleConstraint, filtersListConstraint)
//        sortLabel.setMinSize(25.0, 25.0)
//        sortLabel.setMaxSize(50.0, 50.0)
//        grid.children.add(sortLabel)
//        grid.spacing = 5.0

        this.background = Background(BackgroundFill(mainBackGroundColor, null, null))


        children.add(grid)
        // register with the model when we're ready to start receiving data
        model.addView(this)
    }
}