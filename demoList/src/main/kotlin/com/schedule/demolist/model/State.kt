package com.schedule.demolist.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class State {
    var handler = MainHandler()
    var curIdx = 0


    fun createMemento(): String {
        return Json.encodeToString(this)
    }

    fun restoreMemento(s:String) {
        val tmp = Json.decodeFromString<State>(s)
        this.handler.loadStateUndo(tmp.handler)
        this.curIdx = tmp.curIdx
    }
}
