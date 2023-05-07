package com.schedule.demolist.model

import java.util.*

internal class Caretaker {
    private var mainUndo = LinkedList<String>()
    private var mainRedo = LinkedList<String>()

    private fun add(stack: LinkedList<String>, s: String) {
        if (stack.size > 0 && stack.last == s) {
            return
        }
        stack.addLast(s)
    }
    fun mainPush(state: State) {
        if (mainUndo.size == 500) {
            mainUndo.removeFirst()
        }
        add(mainUndo, state.createMemento())
        mainRedo.clear()
    }

    fun mainPush(str: String) {
        if (mainUndo.size == 500) {
            mainUndo.removeFirst()
        }
        add(mainUndo, str)
        mainRedo.clear()
    }
    fun mainUndoAndRestore(state: State) {
        if (mainUndo.size == 0) {
            return
        }
        val s = mainUndo.removeLast()
        add(mainRedo, state.createMemento())
        state.restoreMemento(s)
    }

    fun mainRedoAndRestore(state: State) {
        if (mainRedo.size == 0) {
            return
        }
        val s = mainRedo.removeLast()
        add(mainUndo, state.createMemento())
        state.restoreMemento(s)
    }
}