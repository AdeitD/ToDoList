package com.schedule.demolist

import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.io.File
import java.nio.file.Paths
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.math.abs

enum class Mode {
    LOGIN, VIEWING, EDIT, ADD, TAG, SEARCH, CHANGE_DATA, TAGLIST
}

enum class Focus {
    TITLE, NOTIFY, END_DATE, PRIORITY, TAGS, DESCRIPTION, OUTER, USERNAME, PASSWORD
}

enum class LoginMode {
    LOGIN, REGISTER
}

enum class SortBy {
    ID, PRIORITY, NAME, END_DATE, CUSTOM
}

enum class TagMode {
    ADD, DELETE, FILTER, DEFAULT
}

enum class ChangeMode {
    CHANGE_USER, CHANGE_DETAILS
}

enum class FilterBy {
    TAG, PRIORITY, NAME, END_DATE, VALUE, DEFAULT
}

val REGULAR_BORDER = Border(BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii(3.0), BorderWidths(2.0)))
val SELECT_BORDER = Border(BorderStroke(Color.ANTIQUEWHITE, BorderStrokeStyle.SOLID, CornerRadii(3.0), BorderWidths(2.0)))
val NO_BORDER = Border(BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.NONE, CornerRadii(0.0), BorderWidths(0.0)))

// change this from a const so we can later make this dynamic, this is just for starting
const val SCREEN_WIDTH = 400.0
const val SCREEN_HEIGHT = 600.0

const val APPLICATION_LOGO = "logo.png"


// EDIT MODE

// These values are based on the min screen size of SCREEN_WIDTH AND HEIGHT
// and the max screen size of 1920 by 1009 which is full screen
const val editViewMinGridWidth = 310.0
const val editViewMaxGridWidth = 1520.0
const val editViewMinGridHeight = 461.0
const val editViewMaxGridHeight = 1636.0

val editViewTextColor: Color = Color.WHITE
val SearchViewColor: Color = Color.WHITE

val editViewColor: Color = Color.BLACK
val editViewBorder: Color = Color.WHITE
val cardViewColor: Color = Color.web("#2a2a2a")
val mainBackGroundColor: Color = Color.BLACK

fun constructFilePath(filename: String):String {
    val localDirectory = Paths.get(System.getProperty("user.home"), ".todo").toString()
    val folder = File(localDirectory)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return Paths.get(localDirectory, filename).toString()
}

const val datePattern = "yyyy/MM/dd"
const val timeBetweenSequentialNotifications:Long = 2000
const val timeToNotificationCheck:Long = 60000
fun validateTime(candidateTime:String): Boolean {
    try {
        LocalTime.parse(candidateTime)
    } catch (e: Exception) {
        return false
    }
    return true
}
fun validateDate(candidateDate:String): Boolean {
    val sdf: DateFormat = SimpleDateFormat(datePattern)
    sdf.isLenient = false
    try {
        sdf.parse(candidateDate)
    } catch (e: ParseException) {
        return false
    }
    return true
}

fun toElapsed(t:LocalTime): Int {
    return t.hour*60 + t.minute
}

fun isNow(date:String, time:String): Boolean {
    val sdf: DateFormat = SimpleDateFormat(datePattern)
    sdf.isLenient = false
    return try {
        val dateParsed = sdf.parse(date)
        val todayDate = Date()
        val timeParsedToElapsed = toElapsed(LocalTime.parse(time))
        val todayNowToElapsed = toElapsed(LocalTime.now())
        sdf.format(dateParsed).equals(sdf.format(todayDate)) && abs(timeParsedToElapsed - todayNowToElapsed) < 5

    } catch (e: Exception) {
        false
    }
}