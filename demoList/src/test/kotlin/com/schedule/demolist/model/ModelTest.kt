package com.schedule.demolist.model

import io.ktor.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.lang.reflect.Method

internal class ModelTest {
    private val model: Model = Model()

    @Test
    fun addTag() {
        assert(model.getTagsList().size == 0)
        model.addTag("firstTag")
        var tags = HashSet<String>(listOf("firstTag"))
        println(model.getTagsList())
        assert(model.getTagsList() == tags)

        model.addTag(("SecondTag"))
        tags.add("SecondTag")
        assert(model.getTagsList() == tags)
    }

    @Test
    fun deleteTag() {
        assert(model.getTagsList().size == 0)
        model.addTag("firstTag")
        var tags = HashSet<String>(listOf("firstTag"))
        println(model.getTagsList())
        assert(model.getTagsList() == tags)

        model.addTag(("SecondTag"))
        tags.add("SecondTag")
        assert(model.getTagsList() == tags)

        model.deleteTag("firstTag")
        var removedTag = HashSet<String>(listOf("SecondTag"))
        assert(model.getTagsList() == removedTag)

        var removedTag2 = HashSet<String>(listOf())
        model.deleteTag("SecondTag")
        assert(model.getTagsList() == removedTag2)
        assert(model.getTagsList().size == 0)
    }

    @Test
    fun cut() {
        val privateCut: Method = Model::class.java.getDeclaredMethod("cutSelectedTask")
        privateCut.isAccessible = true
        val privatePaste: Method = Model::class.java.getDeclaredMethod("pasteSelectedTask")
        privatePaste.isAccessible = true
        val handler = model.state.handler

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

        handler.idCounter = 3
        var listOfCompleted = ArrayList<Task>(listOf(validTask1,validTask2,validTask3))
        handler.testList(listOfCompleted)
        assert(handler.taskList.size == 3)
        println(handler.taskList)

        model.state.curIdx = 1
        privateCut.invoke(model)
        assert(handler.taskList.size == 2)
        assert(handler.taskList == ArrayList<Task>(listOf(validTask1, validTask3)))
        println(handler.taskList)

        model.state.curIdx = 1
        privatePaste.invoke(model)
        assert(handler.taskList.size == 3)
        var finalPaste = ArrayList<Task>(listOf(validTask1, validTask3, validTask2))
        finalPaste[2].id = 3
        finalPaste[2].done = false
        assert(sameTasks(handler.filteredList, finalPaste))
    }

    @Test
    fun copy() {
        val privateCopy: Method = Model::class.java.getDeclaredMethod("copySelectedTask")
        privateCopy.isAccessible = true
        val privatePaste: Method = Model::class.java.getDeclaredMethod("pasteSelectedTask")
        privatePaste.isAccessible = true
        val handler = model.state.handler

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

        handler.idCounter = 3
        var listOfCompleted = ArrayList<Task>(listOf(validTask1,validTask2,validTask3))
        handler.testList(listOfCompleted)
        assert(handler.taskList.size == 3)
        println(handler.taskList)

        model.state.curIdx = 1
        privateCopy.invoke(model)
        assert(handler.taskList.size == 3)
        assert(handler.taskList == listOfCompleted)

        model.state.curIdx = 2
        privatePaste.invoke(model)
        assert(handler.taskList.size == 4)
        var finalPaste = ArrayList<Task>(listOf(validTask1, validTask2, validTask3, validTask2))
        finalPaste[3].id = 3
        finalPaste[3].done = false
        assert(sameTasks(handler.filteredList, finalPaste))
    }

    @Test
    fun undoAndRedo() {
        val privateCaretaker: Field = Model::class.java.getDeclaredField("caretaker")
        privateCaretaker.isAccessible = true
        assert(privateCaretaker.get(model) is Caretaker)

        val privateDeleteCompleted: Method = Model::class.java.getDeclaredMethod("deleteCompletedTasks")
        privateDeleteCompleted.isAccessible = true

        // This is the test for deleting completed tasks
        val handler = model.state.handler

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
        var listOfCompleted = ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5))
        handler.testList(listOfCompleted)
        assert(handler.taskList.size == 5)

        privateDeleteCompleted.invoke(model)
        assert(handler.taskList.size == 0)

        (privateCaretaker.get(model) as Caretaker).mainUndoAndRestore(model.state)
        listOfCompleted = ArrayList<Task>(listOf(validTask1,validTask2,validTask3,validTask4,validTask5))
        assert(handler.taskList.size == 5)
        assert(sameTasks(handler.taskList, listOfCompleted))

        (privateCaretaker.get(model) as Caretaker).mainRedoAndRestore(model.state)
        assert(handler.taskList.size == 0)
        assert(handler.taskList == ArrayList<Task>(listOf()))
    }

    private fun sameTasks(taskList: ArrayList<Task>, listOfCompleted: ArrayList<Task>): Boolean {
        if (taskList.size != listOfCompleted.size) return false
        println("starting loop")
        for (i in 0 until taskList.size) {
            if (!(taskList[i].equals(listOfCompleted[i]))) return false
        }
        return true
    }

    // add and delete tag tests will also test taglist
    @Test
    fun testAddTag() {
        assert(model.getTagsList() == HashSet<String>(listOf()))
        model.addTag("hello")
        assert(model.getTagsList().size == 1)
        assert(model.getTagsList() == HashSet<String>(listOf("hello")))
        model.addTag("hello")
        assert(model.getTagsList().size == 1)
        assert(model.getTagsList() == HashSet<String>(listOf("hello")))
    }

    @Test
    fun testDeleteTag() {
        assert(model.getTagsList() == HashSet<String>(listOf()))
        model.addTag("hello")
        assert(model.getTagsList().size == 1)
        model.addTag("world!")
        assert(model.getTagsList().size == 2)
        assert(model.getTagsList() == HashSet<String>(listOf("hello", "world!")))
        model.deleteTag("hello")
        assert(model.getTagsList().size == 1)
        assert(model.getTagsList() == HashSet<String>(listOf("world!")))
        model.deleteTag("world!")
        assert(model.getTagsList().size == 0)
        assert(model.getTagsList() == HashSet<String>(listOf()))
        model.addTag("this")
        assert(model.getTagsList().size == 1)
        assert(model.getTagsList() == HashSet<String>(listOf("this")))
        model.deleteTag("that")
        assert(model.getTagsList().size == 1)
        assert(model.getTagsList() == HashSet<String>(listOf("this")))
    }

}