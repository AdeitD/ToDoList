package com.schedule.demolist.model

import com.schedule.demolist.*
import com.schedule.demolist.persistence.*
import javafx.scene.input.KeyCode
import java.util.*
import kotlin.collections.ArrayList
import com.schedule.demolist.trie.*
import java.awt.TrayIcon
import javax.swing.SwingUtilities
class Model() {
    private val views: ArrayList<IView> = ArrayList()

    private var clipBoard: Task? = null
    // state management
    private var caretaker = Caretaker()
    var state = State()
    val dbLayer = PersistenceLayer()
    val tagTrie = Trie()
    var curMode = Mode.LOGIN
    var curFocus = Focus.OUTER
    var whichLogin = LoginMode.LOGIN
    var whichTag = TagMode.DEFAULT
    var whichSearch = FilterBy.DEFAULT
    var whichChangeData = ChangeMode.CHANGE_USER

    fun getTagsList(): HashSet<String> {
        return state.handler.tagsList
    }

    fun deleteTag(tag: String) {
        state.handler.deleteTag(tag)
        tagTrie.removeTag(tag)
    }

    fun addTag(tag: String) {
        state.handler.addTag(tag)
        tagTrie.addTag(tag)
    }

    // method that the views can use to register themselves with the Model
    // once added, they are told to update and get state from the Model
    fun addView(view: IView) {
        views.add(view)
        view.updateView()
    }
    fun reminderNotify(trayIcon: TrayIcon) {
        state.handler.taskList.forEach { t ->
            if (isNow(t.endDate, t.notifyTime) && !t.notified) {
                t.notified = true
                SwingUtilities.invokeLater {
                    trayIcon.displayMessage(
                        "${t.header}, ${t.endDate}",
                        t.summary,
                        TrayIcon.MessageType.INFO
                    )
                }
            }
        }
    }

    // the model uses this method to notify all of the Views that the data has changed
    // the expectation is that the Views will refresh themselves to display new data when appropriate
    private fun notifyObservers() {
        for (view in views) {
            view.updateView()
        }
    }

    fun windowResizeWidth() {
        for (view in views) {
            view.resizeWindowWidth()
        }
    }

    fun windowResizeHeight() {
        for (view in views) {
            view.resizeWindowHeight()
        }
    }

    fun stop() {
        dbLayer.onClose(state.handler)
    }

    fun loadState(mh: MainHandler) {
        state.handler.loadState(mh)
        tagTrie.populateFromHashSet(getTagsList())
    }

    var inputBuffer = ""
    var shiftPressed = false
    var upPressed = false
    var downPressed = false
    var altPressed = false

    private fun setIdx(newIdx:Int) {
        if (newIdx < 0 || newIdx >= state.handler.filteredList.size) {
            if (state.curIdx < 0 || state.curIdx >= state.handler.filteredList.size) {
                state.curIdx = 0
            }
            return
        }
        state.curIdx = newIdx
    }

    fun getIdx(): Int { return state.curIdx }

    fun getTasks(): ArrayList<Task> { return state.handler.filteredList }

    fun getAppliedFilters(): ArrayList<Pair<FilterBy, String>> { return state.handler.appliedFilters }

    private fun <R> curryUndo(check:() -> Boolean = { true }, block:() -> R) {
        if (check()) {
            val preState = state.createMemento()
            block()
            val postState = state.createMemento()
            if (preState != postState) {
                caretaker.mainPush(preState)
            }
        }
    }

    fun getSort(): SortBy { return state.handler.appliedSort }

    private fun toggleCompleteOnSelectedTask() = curryUndo (
        { state.handler.filteredList.size != 0 },
        { state.handler.toggleDoneTask(state.handler.filteredList[state.curIdx].id) }
    )

    private fun deleteSelectedTask() = curryUndo (
        { state.handler.filteredList.size != 0 },
        {
            val id = state.handler.filteredList[state.curIdx].id
            state.handler.deleteTask(id)
            setIdx(state.curIdx-1)
        }
    )

    private fun duplicateSelectedTask() = curryUndo (
        { state.handler.filteredList.size != 0 },
        {
            val id = state.handler.filteredList[state.curIdx].id
            state.handler.duplicateTask(id)
            setIdx(state.handler.filteredList.size-1)
        }
    )

    private fun copySelectedTask() = curryUndo (
        { state.handler.filteredList.size != 0 },
        {
            val taskToCopy = state.handler.filteredList[state.curIdx]
            clipBoard = Task(
                taskToCopy.header,
                taskToCopy.endDate,
                taskToCopy.summary,
                taskToCopy.priority,
                ArrayList(taskToCopy.tags))
        }
    )

    private fun cutSelectedTask() = curryUndo (
        { state.handler.filteredList.size != 0 },
        {
            val taskToCopy = state.handler.filteredList[state.curIdx]
            clipBoard = Task(
                taskToCopy.header,
                taskToCopy.endDate,
                taskToCopy.summary,
                taskToCopy.priority,
                ArrayList(taskToCopy.tags))
            deleteSelectedTask()
        }
    )

    private fun pasteSelectedTask() = curryUndo (
        { clipBoard != null },
        {
            state.handler.pasteTask(clipBoard!!, state.curIdx)
            setIdx(state.curIdx + 1)
        }
    )

    private fun deleteCompletedTasks() = curryUndo { // check for errors on edge cases
        var idx = state.curIdx
        state.handler.deleteCompletedTasks()
        if (idx >= state.handler.taskList.size) setIdx(state.handler.taskList.size - 1)
    }

    private fun addTask() = curryUndo {
        state.handler.addTask() // this should add the task on both lists
        setIdx(state.handler.filteredList.size-1)
    }

    private fun reorderTask(curIdx: Int, upHuh: Boolean) = curryUndo {
        state.handler.reorderTask(curIdx, upHuh)
        setIdx(if (upHuh) state.curIdx - 1 else state.curIdx + 1)
    }

    private fun sortTasksAscending(sb: SortBy, inv: Boolean) = curryUndo { state.handler.sortTasksAscending(sb, inv) }

    private fun switchToEdit() {
        if (state.handler.filteredList.size != 0) {
            caretaker.mainPush(state)
            curMode = Mode.EDIT
            curFocus = Focus.OUTER
        }
    }

    private fun switchToView() = curryUndo {
        curMode = Mode.VIEWING
        inputBuffer = "" //wipe input buffer
    }

    fun filterTasks(fb: FilterBy, tag: String) = curryUndo {
        state.handler.filterTasks(fb, tag)
    }

    fun keyReleased(letter: KeyCode) {
        if (letter == KeyCode.SHIFT){ shiftPressed = false}
        else if (letter == KeyCode.UP) { upPressed = false}
        else if (letter == KeyCode.DOWN) {downPressed = false}
        else if (letter == KeyCode.ALT) {altPressed = false}
    }
    fun keyDispatch(letter: KeyCode) {
        if (curMode == Mode.VIEWING) {
            if (letter == KeyCode.SHIFT) { shiftPressed = true }
            else if (letter == KeyCode.UP) {
                upPressed = true
                downPressed = false
            }
            else if (letter == KeyCode.DOWN) {
                downPressed = true
                upPressed = false
            } else if (letter == KeyCode.ALT) {
                altPressed = true
            }
            if (letter == KeyCode.ESCAPE){
                inputBuffer = ""
            } else if (shiftPressed && (upPressed || downPressed)){
                if (upPressed){
                    reorderTask(state.curIdx, true)
                } else {
                    reorderTask(state.curIdx, false)
                }
            } else if (inputBuffer == ""){
                if (letter == KeyCode.J) {
                    setIdx(state.curIdx+1)
                } else if (letter == KeyCode.K) {
                    setIdx(state.curIdx-1)
                } else if (letter == KeyCode.A) {
                    addTask()
                } else if (letter == KeyCode.D) {
                    deleteSelectedTask()
                } else if (letter == KeyCode.M) {
                    duplicateSelectedTask()
                } else if (letter == KeyCode.Q) {
                    toggleCompleteOnSelectedTask()
                }  else if (letter == KeyCode.X) {
                    cutSelectedTask()
                } else if (letter == KeyCode.C) {
                    copySelectedTask()
                } else if (letter == KeyCode.V) {
                    pasteSelectedTask()
                } else if (letter == KeyCode.B) {
                    deleteCompletedTasks()
                } else if (letter == KeyCode.E) {
                    switchToEdit()
                } else if (letter == KeyCode.Z) {
                    caretaker.mainUndoAndRestore(state)
                } else if (letter == KeyCode.Y) {
                    caretaker.mainRedoAndRestore(state)
                } else if (letter == KeyCode.F || letter == KeyCode.S || letter == KeyCode.T){
                    inputBuffer = letter.toString()
                } else if (letter == KeyCode.U) {
                    curMode = Mode.CHANGE_DATA
                    whichChangeData = ChangeMode.CHANGE_USER
                } else if (letter == KeyCode.P) {
                    curMode = Mode.CHANGE_DATA
                    whichChangeData = ChangeMode.CHANGE_DETAILS
                }
            } else {
                if (inputBuffer == "S"){
                    if (!shiftPressed) { //Normal Sort
                        if (letter == KeyCode.C) {
                            caretaker.mainPush(state)
                            sortTasksAscending(SortBy.ID, false)
                        } else if (letter == KeyCode.P) {
                            sortTasksAscending(SortBy.PRIORITY, false)
                        } else if (letter == KeyCode.N) {
                            sortTasksAscending(SortBy.NAME, false)
                        } else if (letter == KeyCode.D) {
                            sortTasksAscending(SortBy.END_DATE, false)
                        }
                    } else if (letter == KeyCode.C) {
                        sortTasksAscending(SortBy.ID, true)
                    } else if (letter == KeyCode.P) {
                        sortTasksAscending(SortBy.PRIORITY, true)
                    } else if (letter == KeyCode.N) {
                        sortTasksAscending(SortBy.NAME, true)
                    } else if (letter == KeyCode.D) {
                        sortTasksAscending(SortBy.END_DATE, true)
                    }
                    inputBuffer = ""
                } else if (inputBuffer == "T"){
                    if (letter == KeyCode.C) {
                        curMode = Mode.TAG
                        whichTag = TagMode.ADD
                    } else if (letter == KeyCode.D) {
                        curMode = Mode.TAG
                        whichTag = TagMode.DELETE
                    } else if (letter == KeyCode.L) {
                        curMode = Mode.TAGLIST
                    }
                    inputBuffer = ""
                } else if (inputBuffer.startsWith("F")){
                    if (inputBuffer.length == 1){
                        caretaker.mainPush(state)
                        if (letter == KeyCode.C) {
                            state.handler.clearFilter()
                        } else if (letter == KeyCode.T) {
                            curMode = Mode.TAG
                            whichTag = TagMode.FILTER
                        } else if (letter == KeyCode.V) {
                            curMode = Mode.SEARCH
                            whichSearch = FilterBy.VALUE
                        } else if (letter == KeyCode.P) {
                            curMode = Mode.SEARCH
                            whichSearch = FilterBy.PRIORITY
                        } else if (letter == KeyCode.D) {
                            curMode = Mode.SEARCH
                            whichSearch = FilterBy.END_DATE
                        } else if (letter == KeyCode.N) {
                            curMode = Mode.SEARCH
                            whichSearch = FilterBy.NAME
                        }
                        inputBuffer = ""
                    } else { inputBuffer = "" }
                }
            }
        } else if (curMode == Mode.EDIT) {
            if (inputBuffer == ""){
                if (letter == KeyCode.H) {
                    curFocus = Focus.TITLE
                } else if (letter == KeyCode.S) {
                    curFocus = Focus.DESCRIPTION
                } else if (letter == KeyCode.T) {
                    curFocus = Focus.TAGS
                } else if (letter == KeyCode.P) {
                    curFocus = Focus.PRIORITY
                } else if (letter == KeyCode.ESCAPE && curFocus != Focus.OUTER) {
                    curFocus = Focus.OUTER
                } else if (letter == KeyCode.ESCAPE) {
                    switchToView()
                } else if (letter == KeyCode.D) {
                    curFocus = Focus.END_DATE
                } else if (letter == KeyCode.N) {
                    curFocus = Focus.NOTIFY
                } else { inputBuffer = "" }
            } else { inputBuffer = "" }
        } else if (curMode == Mode.TAG || curMode == Mode.SEARCH || curMode == Mode.TAGLIST) {
            if (letter == KeyCode.ESCAPE || letter == KeyCode.ENTER) {
                curMode = Mode.VIEWING
                inputBuffer = ""
            }
        } else if (curMode == Mode.LOGIN) {
            if (letter == KeyCode.U) {
                curFocus = Focus.USERNAME
            } else if (letter == KeyCode.P) {
                curFocus = Focus.PASSWORD
            } else if (letter == KeyCode.ESCAPE && curFocus != Focus.OUTER) {
                curFocus = Focus.OUTER
            } else if (letter == KeyCode.ENTER) {
                curMode = Mode.VIEWING
            } else if (letter == KeyCode.R) {
                whichLogin = LoginMode.REGISTER
            } else if (letter == KeyCode.L) {
                whichLogin = LoginMode.LOGIN
            }
            inputBuffer = ""
        } else if (curMode == Mode.CHANGE_DATA) {
            if (letter == KeyCode.ESCAPE) {
                curMode = Mode.VIEWING
                inputBuffer = ""
            }
        }
        notifyObservers()
    }

    fun updateTask(task: Task) {
        state.handler.filteredList[state.curIdx] = task
        notifyObservers()
    }
}
