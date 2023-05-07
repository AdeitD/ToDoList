package com.schedule.demolist.views

import com.schedule.demolist.*
import com.schedule.demolist.model.Model
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.text.SimpleDateFormat
import java.util.*

internal class EditView(
    private val model: Model
) : StackPane(), IView {

    private var grid = GridPane()
    private val back = Rectangle()

    private var titleField = TextField("")

    private var endDateLabel = Label("End Date: ")
    private var endDateField = TextField("")

    private var notifyTimeLabel = Label("Notify Time: ")
    private var notifyTimeField = TextField("")

    private var priorityLabel = Label("Priority: ")
    private var priorityField = TextField("")

    private var tagsLabel = Label("Tags: ")
    private var tagsField = TextField("")

    private var descriptionField = TextArea("" )
    private var curTrieCandidates = ArrayList<String>()
    private var curTrieCandidate = 0
    override fun updateView() {
        if (model.curMode == Mode.EDIT && model.state.handler.filteredList.size > 0) {
            // populate fields with curIdx
            val task = model.state.handler.filteredList[model.state.curIdx]
            titleField.text = task.header
            endDateField.text = task.endDate
            notifyTimeField.text = task.notifyTime
            priorityField.text = task.priority.toString()
            val tagsString = task.tags.toString()
            tagsField.text = tagsString.substring(1,tagsString.length-1)
            descriptionField.text = task.summary
        }
        else if (model.curMode == Mode.ADD) {
            titleField.text = ""
            endDateField.text = ""
            notifyTimeField.text = ""
            priorityField.text = ""
            tagsField.text = ""
            descriptionField.text = ""
        }
        Platform.runLater {
            if (model.curFocus == Focus.TITLE) {
                titleField.requestFocus()
            } else if (model.curFocus == Focus.PRIORITY) {
                priorityField.requestFocus()
            } else if (model.curFocus == Focus.TAGS) {
                tagsField.requestFocus()
            } else if (model.curFocus == Focus.DESCRIPTION) {
                descriptionField.requestFocus()
            } else if (model.curFocus == Focus.OUTER) {
                this.requestFocus()
            } else if (model.curFocus == Focus.END_DATE) {
                model.state.handler.filteredList[model.state.curIdx].notified = false // TODO is this really the only way?
                endDateField.requestFocus()
            } else if (model.curFocus == Focus.NOTIFY) {
                notifyTimeField.requestFocus()
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
        val windowHeight = this.height
        val gradientHeight = editViewMinGridHeight/editViewMaxGridHeight
        val heightFunc = gradientHeight*windowHeight + editViewMinGridHeight - (SCREEN_HEIGHT * gradientHeight)
        grid.minHeight = heightFunc
        grid.maxHeight = heightFunc

        back.y = grid.layoutY - 5
        back.height = grid.minHeight + 10
    }

    private fun setPrompt() {
        titleField.promptText = "Title"
        // date ones are temp if we really need it
        endDateField.promptText = "YYYY/MM/DD"
        notifyTimeField.promptText = "HH:mm"

        priorityField.promptText = "#"
        tagsField.promptText = "tag1, tag2, ..."
        descriptionField.promptText = "Description of Task"
    }

    private fun setProps() {
        descriptionField.minHeight = Region.USE_PREF_SIZE
        descriptionField.minWidth = titleField.width
        descriptionField.isWrapText = true
        descriptionField.focusedProperty().addListener { _, _, isNowFocused ->
            if (isNowFocused) {
                descriptionField.selectAll()
            }
        }

        back.x = grid.layoutX - 5
        back.y = grid.layoutY - 5
        back.width = grid.width + 10
        back.height = grid.height + 10
        back.arcWidth = 20.0
        back.arcHeight = 20.0
        back.stroke = editViewBorder
        back.fill = Color.TRANSPARENT

        endDateLabel.textFill = editViewTextColor
        notifyTimeLabel.textFill = editViewTextColor
        priorityLabel.textFill = editViewTextColor
        tagsLabel.textFill = editViewTextColor

    }

    private fun setGrid() {
        grid.alignment = Pos.CENTER
        grid.setMaxSize(300.0, 300.0)
        grid.hgap = 10.0
        grid.vgap = 10.0
        grid.border = Border(BorderStroke(editViewColor, BorderStrokeStyle.SOLID, CornerRadii(1.0), BorderWidths(10.0)))
        grid.background = Background(BackgroundFill(editViewColor, null, null))
        grid.padding = Insets(25.0, 25.0, 25.0, 25.0)

        grid.add(titleField, 0, 0, 2, 1)

        grid.add(endDateLabel, 0, 1, 1, 1)
        grid.add(endDateField, 1, 1, 1, 1)

        grid.add(notifyTimeLabel, 0, 2, 1, 1)
        grid.add(notifyTimeField, 1, 2, 1, 1)

        grid.add(priorityLabel, 0, 3, 1, 1)
        grid.add(priorityField, 1, 3, 1, 1)

        grid.add(tagsLabel, 0, 4, 1, 1)
        grid.add(tagsField, 1, 4, 1, 1)

        grid.add(descriptionField, 0, 5, 2, 4)
    }
    private fun autoComplete(e: KeyEvent){
        val letter = e.code
        if (letter == KeyCode.TAB){
            e.consume()
            if (curTrieCandidates.size == 0) { return }
            val curText = tagsField.characters
            var curCheck = curText.length -1
            curTrieCandidate = (curTrieCandidate + 1)%curTrieCandidates.size
            while (curCheck >= 0) {
                if (curText[curCheck] == ',') {
                    break
                }
                curCheck -= 1
            }
            if (curCheck == -1){tagsField.text =curTrieCandidates[curTrieCandidate]}
            else{
                val prefix = curText.substring(0, curCheck+1)
                tagsField.text = prefix + curTrieCandidates[curTrieCandidate]
            }
            tagsField.positionCaret(tagsField.text.length)
        }
    }
    private fun getAutofillCandidates(letter: KeyCode){
        if (letter == KeyCode.TAB){return}
        val curText = tagsField.characters
        var curCheck = curText.length -1
        while (curCheck >= 0) {
            if (curText[curCheck] == ',') {
                curCheck += 1
                break
            }
            curCheck -= 1
        }
        var partialTag = curText
        if (curCheck != -1){partialTag = curText.substring(curCheck)}
        curTrieCandidates = model.tagTrie.getCandidates(partialTag.toString())
        curTrieCandidates.add(partialTag.toString())
        curTrieCandidate = curTrieCandidates.size -1
    }

    private fun setEndDate() {
        val formatter = SimpleDateFormat(datePattern)
        val date = Date()
        val cal: Calendar = GregorianCalendar.getInstance()
        cal.time = date
        if (endDateField.text.equals("Today", ignoreCase = true)) endDateField.text = formatter.format(cal.time).toString()
        else if (endDateField.text.equals("Tomorrow", ignoreCase = true)) {
            cal.add( GregorianCalendar.DAY_OF_MONTH, 1 )
            endDateField.text = formatter.format(cal.time).toString()
        } else {
            var mult: Int = 0
            var idx: Int = 0
            while (idx < endDateField.text.length && endDateField.text[idx].isDigit()) {
                mult = mult * 10 + (endDateField.text[idx] - '0')
                idx += 1
            }
            if (idx < endDateField.text.length) {
                if (mult == 0) mult += 1
                var typeChar: Char = endDateField.text[idx]
                var typeShift: Int
                if (typeChar.equals('d', ignoreCase = true)) {
                    typeShift = GregorianCalendar.DAY_OF_YEAR
                } else if (typeChar.equals('w', ignoreCase = true)) {
                    typeShift = GregorianCalendar.WEEK_OF_MONTH
                } else if (typeChar.equals('m', ignoreCase = true)) {
                    typeShift = GregorianCalendar.MONTH
                } else if (typeChar.equals('y', ignoreCase = true)) {
                    typeShift = GregorianCalendar.YEAR
                } else return
                cal.add( typeShift, mult )
            } else if (mult != 0) {
                cal.add( GregorianCalendar.DAY_OF_YEAR, mult )
            } else  {
                return
            }
            endDateField.text = formatter.format(cal.time).toString()
        }
    }

    private fun convertTime() {
        var testTime:String = notifyTimeField.text
        val companies: List<String> = testTime.split(":")
        if (companies.size != 2) return
        if (companies[0].length == 1) notifyTimeField.text = "0${notifyTimeField.text}"
    }

    private fun checkDateBlank() {
        val formatter = SimpleDateFormat(datePattern)
        val date = Date()
        val cal: Calendar = GregorianCalendar.getInstance()
        if (endDateField.text == "" && notifyTimeField.text != "") {
            cal.time = date
            endDateField.text = formatter.format(cal.time).toString()
        } else if (notifyTimeField.text == "" && endDateField.text != "") {
            notifyTimeField.text = "12:00"
        }
    }

    init {
        setPrompt()
        setProps()
        setGrid()
        tagsField.setOnKeyReleased {e-> getAutofillCandidates(e.code) }
        tagsField.addEventFilter(KeyEvent.KEY_PRESSED) { e -> autoComplete(e) }
        this.setOnKeyPressed {
            if (model.curFocus != Focus.OUTER) {
                val myTask = model.getTasks()[model.getIdx()]
                myTask.header = titleField.text.lowercase(Locale.getDefault())
                myTask.summary = descriptionField.text.lowercase(Locale.getDefault())
                val priority = priorityField.text.toIntOrNull()
                if (priority != null) {
                    myTask.priority = priority
                }

                setEndDate()
                convertTime()
                checkDateBlank()
                myTask.notifyTime = if (validateTime(notifyTimeField.text)) notifyTimeField.text else ""
                myTask.endDate = if (validateDate(endDateField.text)) endDateField.text else ""

                val tagsFieldList = tagsField.text.split(',')
                val tagsHashSet = HashSet<String>()
                tagsFieldList.forEach { t ->
                    val newT = t.replace(" ","").lowercase(Locale.getDefault())
                     if (model.getTagsList().contains(newT)) {
                         tagsHashSet.add(newT)
                     }
                }

                myTask.tags = tagsHashSet

                model.updateTask(myTask)

            }
        }

        this.children.add(grid)
        this.children.add(back)
        model.addView(this)
    }
}