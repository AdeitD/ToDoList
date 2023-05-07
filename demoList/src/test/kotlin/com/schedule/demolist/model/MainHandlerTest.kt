package com.schedule.demolist.model

import com.schedule.demolist.SortBy
import com.schedule.demolist.trie.Trie
import org.junit.jupiter.api.Test

internal class MainHandlerTest {
    private val handler = MainHandler()

    fun compareTaskArray(expected:ArrayList<Task>, produced:ArrayList<Task> ):Boolean{
        if (expected.size != produced.size){return false}
        for (i in 0 until expected.size){
            if (!expected[i].equals(produced[i])){
                return false
            }
        }
        return true
    }

    @Test
    fun addTaskToEmptyList(){
        var emptyTaskList  = ArrayList<Task>()
        handler.taskList = emptyTaskList

        var expectedTask = Task("head", "2022/12/05",
            "why",-5,
            ArrayList<String>(listOf("league", "of", "legend")))
        expectedTask.id = 0
        var expectedList = ArrayList<Task>(listOf(expectedTask))

        handler.addTask("head", "2022/12/05", "",
            "why",-5,
            ArrayList<String>(listOf("league", "of", "legend")))
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun addTaskToNonEmptyList() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12", "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -0, tagList2)
        //TODO we need this to autosort when a mid priority is added as it currently just puts it at the back
        var expectedList = ArrayList<Task>(listOf(validTask, validTask2, validTask3))
        validTask3.id = 2
        handler.idCounter = 2;

        handler.testList(ArrayList<Task>(listOf(validTask,validTask2)))
        handler.addTask("", "", "", "", 0, tagList2)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun editValidId() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12", "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -0, tagList2)
        validTask3.id = 2
        var newTask3 = Task("find a job", "End of term",
            "Please waterloo works work for once im on my knees please", 2, tagList2)
        newTask3.id = 2

        var expectedList = ArrayList<Task>(listOf(validTask, validTask2, newTask3))
        handler.taskList = ArrayList<Task>(listOf(validTask,validTask2, validTask3))
        handler.idCounter = 3;
        handler.editTask(2, "find a job", "End of term", "",
            "Please waterloo works work for once im on my knees please", 2, null)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun reorderDown() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12", "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2

        var expectedList = ArrayList<Task>(listOf(validTask2,validTask, validTask3))

        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.reorderTask(0, false)
        var producedList = handler.filteredList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun reorderUp(){
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        handler.idCounter = 3;

        var expectedList = ArrayList<Task>(listOf(validTask2,validTask,validTask3))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2,validTask3)))
        handler.reorderTask(1, true)
        var producedList = handler.filteredList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun reorderNoWhere() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12", "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        handler.idCounter = 3;

        var expectedList = ArrayList<Task>(listOf(validTask,validTask2, validTask3))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.reorderTask(2, false)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
        handler.reorderTask(0, true)
        producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun deleteValid() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        handler.idCounter = 3;
        var expectedList = ArrayList<Task>(listOf(validTask2, validTask3))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.deleteTask(0)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))

        expectedList = ArrayList<Task>(listOf(validTask2))
        handler.deleteTask(2)
        producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))

        expectedList = ArrayList<Task>()
        handler.deleteTask(1)
        producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun duplicateMid() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2.id  = 1
        var validTask2Copy = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2Copy.id  = 3
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        handler.idCounter = 3;
        var expectedList = ArrayList<Task>(listOf(validTask,validTask2, validTask3, validTask2Copy))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.duplicateTask(1)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun duplicateEnd(){
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        var validTask3Copy = Task("", "", "", -5, tagList2)
        validTask3Copy.id  = 3
        handler.idCounter = 3;
        var expectedList = ArrayList<Task>(listOf(validTask,validTask2, validTask3, validTask3Copy))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.duplicateTask(2)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun deleteCompleted() {
        val tagList1 = ArrayList<String>(listOf("j","*","f","f"))

        val validTask1 = Task("task 1", "2022/01/01", "", 5, tagList1)
        validTask1.id  = 0
        validTask1.done = true

        val validTask2 = Task("aaaaa", "2022/11/23", "", -5, tagList1)
        validTask2.id  = 1
        validTask2.done = true

        val validTask3 = Task("zzzzz", "2023/01/01/", "", 3, tagList1)
        validTask3.id  = 2
        validTask3.done = true

        val validTask4 = Task("be for real", "2023/02/01", "", -2, tagList1)
        validTask4.id  = 3
        validTask4.done = true

        val validTask5 = Task("task 5", "2022/06/03", "", 3, tagList1)
        validTask5.id  = 4
        validTask5.done = true

        handler.idCounter = 5;
        handler.testList(ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5)))
        assert(handler.taskList.size == 5)

        handler.deleteCompletedTasks()
        assert(handler.taskList.size == 0)
    }

    @Test
    fun finishValid() {
        var tagList1 = ArrayList<String>(listOf("j","*","f","f"))
        var tagList2 = ArrayList<String>(listOf("gamer", "sadness"))

        var validTask = Task("Bus down", "tomorrow",
            "I hate it here", 2, ArrayList<String>()
        )
        validTask.id = 0
        var validTask2 = Task("learn how to dougie", "12/03/12",
            "", -4, tagList1)
        validTask2.id  = 1
        var validTask3 = Task("", "", "", -5, tagList2)
        validTask3.id = 2
        var validTask3Done = Task("", "", "", -5, tagList2)
        validTask3Done.id = 2
        validTask3Done.done = true
        handler.idCounter = 3;
        var expectedList = ArrayList<Task>(listOf(validTask,validTask2, validTask3Done))
        handler.testList(ArrayList<Task>(listOf(validTask,validTask2, validTask3)))
        handler.toggleDoneTask(2)
        var producedList = handler.taskList
        assert(compareTaskArray(expectedList, producedList))
    }

    @Test
    fun sortIDTesting() {
        val tagList1 = ArrayList<String>(listOf("j","*","f","f"))

        val validTask1 = Task("task 1", "2022/01/01", "", 5, tagList1)
        validTask1.id  = 0

        val validTask2 = Task("aaaaa", "2022/11/23", "", -5, tagList1)
        validTask2.id  = 1

        val validTask3 = Task("zzzzz", "2023/01/01/", "", 3, tagList1)
        validTask3.id  = 2

        val validTask4 = Task("be for real", "2023/02/01", "", -2, tagList1)
        validTask4.id  = 3

        val validTask5 = Task("task 5", "2022/06/03", "", 3, tagList1)
        validTask5.id  = 4

        handler.idCounter = 5;
        handler.testList(ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5)))

        val expIDSort = ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5))
        val expIDSortReverse = ArrayList<Task>(listOf(validTask5,validTask4,validTask3,validTask2,validTask1))

        handler.sortTasksAscending(SortBy.ID,false)
        assert(compareTaskArray(expIDSort, handler.filteredList))

        handler.sortTasksAscending(SortBy.ID,true)
        assert(compareTaskArray(expIDSortReverse, handler.filteredList))
    }

    @Test
    fun sortPrioTesting() {
        val tagList1 = ArrayList<String>(listOf("j","*","f","f"))

        val validTask1 = Task("task 1", "2022/01/01", "", 5, tagList1)
        validTask1.id  = 0

        val validTask2 = Task("aaaaa", "2022/11/23", "", -5, tagList1)
        validTask2.id  = 1

        val validTask3 = Task("zzzzz", "2023/01/01/", "", 3, tagList1)
        validTask3.id  = 2

        val validTask4 = Task("be for real", "2023/02/01", "", -2, tagList1)
        validTask4.id  = 3

        val validTask5 = Task("task 5", "2022/06/03", "", 3, tagList1)
        validTask5.id  = 4

        handler.idCounter = 5;
        handler.testList(ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5)))

        val expPrioritySort = ArrayList<Task>(listOf(validTask2,validTask4,validTask5,validTask3,validTask1))
        val expPrioritySortReverse = ArrayList<Task>(listOf(validTask1,validTask5,validTask3,validTask4,validTask2))

        handler.sortTasksAscending(SortBy.PRIORITY,false)
        assert(compareTaskArray(expPrioritySort, handler.filteredList))

        handler.sortTasksAscending(SortBy.PRIORITY,true)
        assert(compareTaskArray(expPrioritySortReverse, handler.filteredList))
    }

    @Test
    fun sortNameTesting() {
        val tagList1 = ArrayList<String>(listOf("j","*","f","f"))

        val validTask1 = Task("task 1", "2022/01/01", "", 5, tagList1)
        validTask1.id  = 0

        val validTask2 = Task("aaaaa", "2022/11/23", "", -5, tagList1)
        validTask2.id  = 1

        val validTask3 = Task("zzzzz", "2023/01/01/", "", 3, tagList1)
        validTask3.id  = 2

        val validTask4 = Task("be for real", "2023/02/01", "", -2, tagList1)
        validTask4.id  = 3

        val validTask5 = Task("task 5", "2022/06/03", "", 3, tagList1)
        validTask5.id  = 4

        handler.idCounter = 5;
        handler.testList(ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5)))

        val expNameSort = ArrayList<Task>(listOf(validTask2,validTask4,validTask1,validTask5,validTask3))
        val expNameSortReverse = ArrayList<Task>(listOf(validTask3,validTask5,validTask1,validTask4,validTask2))

        handler.sortTasksAscending(SortBy.NAME,false)
        assert(compareTaskArray(expNameSort, handler.filteredList))

        handler.sortTasksAscending(SortBy.NAME,true)
        assert(true)
        assert(compareTaskArray(expNameSortReverse, handler.filteredList))
    }

    @Test
    fun sortEndDateTesting() {
        val tagList1 = ArrayList<String>(listOf("j","*","f","f"))

        val validTask1 = Task("task 1", "2022/01/01", "", 5, tagList1)
        validTask1.id  = 0

        val validTask2 = Task("aaaaa", "2022/11/23", "", -5, tagList1)
        validTask2.id  = 1

        val validTask3 = Task("zzzzz", "2023/01/01/", "", 3, tagList1)
        validTask3.id  = 2

        val validTask4 = Task("be for real", "2023/02/01", "", -2, tagList1)
        validTask4.id  = 3

        val validTask5 = Task("task 5", "2022/06/03", "", 3, tagList1)
        validTask5.id  = 4

        handler.idCounter = 5;
        handler.testList(ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5)))

        val expDateSort = ArrayList<Task>(listOf(validTask1,validTask5,validTask2,validTask3,validTask4))
        val expDateSortReverse = ArrayList<Task>(listOf(validTask4,validTask3,validTask2,validTask5,validTask1))

        handler.sortTasksAscending(SortBy.END_DATE,false)
        assert(compareTaskArray(expDateSort, handler.filteredList))

        handler.sortTasksAscending(SortBy.END_DATE,true)
        assert(compareTaskArray(expDateSortReverse, handler.filteredList))
    }

    @Test
    fun testTrie(){
        var t = Trie()
        t.addTag("star")
        t.addTag("starfish")
        t.addTag("starlight")
        t.addTag("starmie")
        t.addTag("starboy")
        t.addTag("rishabh")
        t.removeTag("star")

        var all = ArrayList<String>(listOf("starfish", "starlight", "starmie", "starboy", "rishabh"))
        assert(t.getCandidates("") == all)

        var star = ArrayList<String>(listOf("starfish", "starlight", "starmie", "starboy"))
        assert(t.getCandidates("star") == star)

        var r = ArrayList<String>(listOf("rishabh"))
        assert(t.getCandidates("r") == r)

        t.removeTag("starlight")

        var newStar = ArrayList<String>(listOf("starfish", "starmie", "starboy"))
        assert(t.getCandidates("star") == newStar)

        var newAll = ArrayList<String>(listOf("starfish", "starmie", "starboy", "rishabh"))
        assert(t.getCandidates("") == newAll)
    }
}