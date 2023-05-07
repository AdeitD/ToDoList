package com.schedule.demolist.model

import com.schedule.demolist.FilterBy
import com.schedule.demolist.SortBy
import kotlinx.serialization.Serializable

@Serializable
class MainHandler() {
    var idCounter = 0
    var taskList = ArrayList<Task>()
    var filteredList = ArrayList<Task>()
    var appliedFilters = ArrayList<Pair<FilterBy, String>>()
    var appliedSort: SortBy = SortBy.ID
    var invertSort: Boolean = false
    var customSort: HashMap<Int, Int> = HashMap()
    var tagsList = HashSet<String>()

    fun addTask(header: String ="",
                endDate: String ="",
                notifyTime: String ="",
                summary: String = "",
                priority: Int = 0,
                tags: ArrayList<String> = ArrayList()): Int {
        val newTask = Task()
        newTask.id = idCounter
        idCounter += 1
        taskList.add(newTask)
        editTask(
            newTask.id,
            header,
            endDate,
            notifyTime,
            summary,
            priority,
            tags
        )
        return newTask.id
    }

    fun editTask(id: Int,
                 header: String? = null,
                 endDate: String? = null,
                 notifyTime: String? = null,
                 summary: String? = null,
                 priority: Int? = null,
                 tags: ArrayList<String>? = null) {
        for (task in taskList) {
            if (task.id == id) {
                if (header != null) {
                    task.header = header
                }
                if (endDate != null) {
                    task.endDate = endDate
                }
                if (notifyTime != null) {
                    task.notifyTime = notifyTime
                }
                if (summary != null) {
                    task.summary = summary
                }
                if (priority != null) {
                    task.priority = priority
                }
                if (tags != null) {
                    task.tags = HashSet(tags)
                }
            }
        }
        refreshFilteredList()
    }

    fun reorderTask(taking: Int, upHuh: Boolean) { // FIX THIS
        if (taking < 0 || taking >= filteredList.size) {
            return
        } else if (!upHuh) {
            if (taking == filteredList.size - 1) {
                return
            }
            var tmp = filteredList[taking]
            filteredList[taking] = filteredList[taking + 1]
            filteredList[taking + 1] = tmp
        } else if (taking != 0) {
            var tmp = filteredList[taking]
            filteredList[taking] = filteredList[taking - 1]
            filteredList[taking - 1] = tmp
        }
        createCustomSortFromCurrentFilteredList()
        appliedSort = SortBy.CUSTOM
        invertSort = false
    }

    fun deleteTask(id:Int) {
        taskList.removeIf{ task -> task.id == id }
        refreshFilteredList()
    }

    fun duplicateTask(id:Int): Int? {
        for (task in taskList) {
            if (task.id == id) {
                val t = addTask(
                    task.header,
                    task.endDate,
                    task.notifyTime,
                    task.summary,
                    task.priority,
                    ArrayList(task.tags)
                )
                return t
            }
        }
        return null
    }

    fun pasteTask(task: Task, idx: Int): Boolean {
        if (!doesPassFilters(task)) { return false }
        createCustomSortFromCurrentFilteredList()
        appliedSort = SortBy.CUSTOM
        invertSort = false
        val t = addTask(
            task.header,
            task.endDate,
            task.notifyTime,
            task.summary,
            task.priority,
            ArrayList(task.tags),
        )
        refreshFilteredList()
        customSort.forEach { (k, v) ->
            if (v > idx) {
                customSort[k] = customSort[k]!! + 1
            }
        }
        customSort[t] = idx+1
        refreshFilteredList()
        return true
    }

    fun deleteCompletedTasks() {
        taskList.removeIf { task -> task.done && filteredList.contains(task) }
        refreshFilteredList()
    }

    fun toggleDoneTask(id:Int) {
        for (task in taskList) {
            if (task.id == id) {
                task.done = !(task.done)
            }
        }
        refreshFilteredList()
    }

    fun sortTasksAscending(sortByWhat: SortBy, invertSort: Boolean) {
        this.appliedSort = sortByWhat
        this.invertSort = invertSort
        if (!invertSort) {
            when (sortByWhat) {
                SortBy.ID -> filteredList.sortBy { it.id }
                SortBy.PRIORITY -> filteredList.sortWith(compareBy<Task> { it.priority }.thenBy { it.header })
                SortBy.NAME -> filteredList.sortBy { it.header }
                SortBy.END_DATE -> filteredList.sortWith(compareBy<Task> { it.endDate }.thenBy { it.header })
                SortBy.CUSTOM -> applyCustomSortToFilteredList()
            }
        } else {
            when (sortByWhat) {
                SortBy.ID -> filteredList.sortByDescending { it.id }
                SortBy.PRIORITY -> {
                    filteredList.sortBy { it.header }
                    filteredList.sortByDescending { it.priority }
                }
                SortBy.NAME -> filteredList.sortByDescending { it.header }
                SortBy.END_DATE -> {
                    filteredList.sortBy { it.header }
                    filteredList.sortByDescending { it.endDate }
                }
                SortBy.CUSTOM -> {
                    applyCustomSortToFilteredList()
                    filteredList.reverse()
                }
            }
        }
    }

    fun addTag(name: String) {
        tagsList.add(name)
    }

    fun deleteTag(name: String) {
        tagsList.remove(name)
    }

    fun filterTasks(filterByWhat: FilterBy, filterValue: String) {
        if (filterByWhat == FilterBy.TAG && !tagsList.contains(filterValue)) { return }
        appliedFilters.add(Pair(filterByWhat, filterValue))
        refreshFilteredList()
    }

    fun clearFilter() {
        appliedFilters.clear()
        refreshFilteredList()
    }

    fun testList(list: ArrayList<Task>) {
        taskList = list
        refreshFilteredList()
    }

    private fun refreshFilteredList() {
        filteredList = listThroughAppliedFilters(taskList)
        sortTasksAscending(appliedSort, invertSort)
    }

    private fun doesPassFilters(t: Task): Boolean {
        var startList = ArrayList<Task>(listOf(t))
        val filList = listThroughAppliedFilters(startList)
        return filList.size > 0
    }

    private fun listThroughAppliedFilters(ls: ArrayList<Task>): ArrayList<Task> {
        var startList = ArrayList<Task>(ls)
        for ((filType, filValue) in appliedFilters) {
            var newFilteredList = ArrayList<Task>()
            when (filType) {
                FilterBy.TAG -> {
                    startList.forEach { t ->
                        if (t.tags.contains(filValue)) {
                            newFilteredList.add(t)
                        }
                    }
                }
                FilterBy.PRIORITY -> {
                    startList.forEach { t ->
                        if (t.priority == filValue.toInt()) {
                            newFilteredList.add(t)
                        }
                    }
                }
                FilterBy.NAME -> {
                    startList.forEach { t ->
                        if (t.header.contains(filValue, true)) {
                            newFilteredList.add(t)
                        }
                    }
                }
                FilterBy.END_DATE -> {
                    startList.forEach { t ->
                        if (t.endDate.equals(filValue, true)) {
                            newFilteredList.add(t)
                        }
                    }
                }
                FilterBy.VALUE -> {
                    startList.forEach { t ->
                        if (t.contains(filValue)) {
                            newFilteredList.add(t)
                        }
                    }
                }
            }
            startList = newFilteredList
        }
        return startList
    }
    private fun createCustomSortFromCurrentFilteredList() {
        val newCustomSort = HashMap<Int, Int>()
        for (i in 0 until filteredList.size) {
            newCustomSort[filteredList[i].id] = i
        }
        customSort = newCustomSort
    }

    private fun applyCustomSortToFilteredList() { // ONLY RUN THIS AFTER RECREATING THE FILTERED LIST
        if (checkAndModifyCustomSortIfGood()) {
            var newFilteredList = ArrayList<Task>()
            for (i in 0 until filteredList.size) newFilteredList.add(Task())
            for (i in 0 until filteredList.size) {
                newFilteredList[customSort[filteredList[i].id]!!] = filteredList[i]
            }
            filteredList = newFilteredList
        } else {
            appliedSort = SortBy.ID
            invertSort = false
        }
    }

    private fun checkAndModifyCustomSortIfGood(): Boolean {
        val a1 = HashSet<Int>(customSort.keys)
        val a2 = HashSet<Int>(filteredList.map { it.id })
        val a3 = a1.minus(a2)
        val a4 = a2.minus(a1)
        if (a3.isEmpty() && a1.size == a2.size) {
            return true
        } else if (a3.size == 1 && a1.size == a2.size + 1) {
            val id = a3.first()
            val idx = customSort[id]!!
            customSort.remove(a3.first())
            customSort.forEach { (k, v) ->
                if (v > idx) {
                    customSort[k] = customSort[k]!! - 1
                }
            }
            return true
        } else if (a3.isEmpty() && a1.size == a2.size - 1) {
            customSort[a4.first()] = filteredList.size-1
            return true
        }
        return false
    }

    fun loadState(state: MainHandler) {
        this.taskList = state.taskList
        this.idCounter = state.idCounter
        this.tagsList = state.tagsList
        this.appliedFilters = state.appliedFilters
        refreshFilteredList()
    }

    fun loadStateUndo(state: MainHandler) {
        this.taskList = state.taskList
        this.idCounter = state.idCounter
        this.tagsList = state.tagsList
        this.appliedFilters = state.appliedFilters
        this.customSort = state.customSort
        this.appliedSort = state.appliedSort
        this.invertSort = state.invertSort
        refreshFilteredList()
    }
}