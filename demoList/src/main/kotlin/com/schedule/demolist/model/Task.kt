package com.schedule.demolist.model

import kotlinx.serialization.Serializable

@Serializable
class Task {
    var id  = 0
    var done = false
    var notified = false
    var header = ""
    var endDate = ""
    var notifyTime = ""
    var summary = ""
    var priority = -5
    var tags = HashSet<String>()
    constructor() {}
    constructor(
        header: String =" ",
        endDate: String =" ",
        summary: String =" ",
        priority: Int = -5,
        tags: ArrayList<String> = ArrayList()){
        this.header = header
        this.notifyTime = notifyTime
        this.endDate = endDate
        this.summary = summary
        this.priority = priority
        this.tags = HashSet<String>()
        for (tag in tags){
            this.tags.add(tag)
        }
    }

    fun contains(filValue: String) : Boolean {
        if (summary.contains(filValue, ignoreCase = true)) return true
        else if (header.contains(filValue, ignoreCase = true)) return true
        else if (notifyTime.toString().contains(filValue, ignoreCase = true)) return true
        else if (endDate.toString().contains(filValue, ignoreCase = true)) return true
        else if (priority.toString().contains(filValue, ignoreCase = true)) return true

        for (tag in tags) {
            if (tag.contains(filValue, ignoreCase = true)) return true
        }

        return false
    }

    fun equals(other: Task): Boolean {
        if (other.done != this.done || other.id != this.id || other.header != this.header
            ||other.notifyTime != this.notifyTime || other.endDate != this.endDate ||
            other.priority != this.priority || other.summary != this.summary|| other.done != this.done
            || other.tags != this.tags
        ){return false}
        return true
    }
}
