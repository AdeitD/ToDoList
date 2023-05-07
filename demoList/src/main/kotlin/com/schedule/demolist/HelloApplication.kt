package com.schedule.demolist

import com.schedule.demolist.model.Model
import com.schedule.demolist.views.ParentView
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.*
import java.awt.event.ActionEvent
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.swing.SwingUtilities


@Serializable
class BuildScene {
    var width = 0.0
    var height = 0.0
    var posx = -10000000.0
    var posy = -10000000.0
    private fun serializeState(): String {
        return Json.encodeToString(this)
    }

    fun saveToJson(fileName: String? = null){
        var filePath = constructFilePath("config.json")
        val serializedState = serializeState()
        File(filePath).writeText(serializedState)
    }

    fun loadFromJson(fileName: String){
        var filePath = constructFilePath("config.json")
        if (!File(filePath).exists()) {
            this.width = SCREEN_WIDTH
            this.height = SCREEN_HEIGHT
            saveToJson()
        }
        //check not null, check can read, check is valid, check is not dir
        val inputStream: InputStream = File(filePath).inputStream()
        var jsonString = inputStream.bufferedReader().use { it.readText() }
        val tmp  = Json.decodeFromString<BuildScene>(jsonString)
        this.width = tmp.width
        this.height = tmp.height
        this.posx = tmp.posx.toDouble()
        this.posy = tmp.posy.toDouble()
    }

    fun updateWidth(width: Double) {
        this.width = width
    }

    fun updateHeight(height: Double) {
        this.height = height
    }

    fun updatePos(x: Double, y: Double) {
        this.posx = x
        this.posy = y
    }

    constructor() {
    }
}

// MVC with coupled View and Controller (a more typical method than MVC1)
// A simple MVC example inspired by Joseph Mack, http://www.austintek.com/mvc/
// This version uses MVC: two views coordinated with the observer pattern, but no separate controller.
class HelloApplication : Application() {

    // create and initialize the Model to hold our counter
    private val model: Model = Model()

    private val iconImageLoc: String? =
        "http://icons.iconarchive.com/icons/scafer31000/bubble-circle-3/16/GameCenter-icon.png"
    private val notificationTimer: Timer = Timer()
    private val timeFormat: DateFormat = SimpleDateFormat.getTimeInstance()

    private var stage = Stage()

    private val parentView  = ParentView(model)
    private val config = BuildScene()
    private lateinit var homeScene : Scene

    fun resizeBoth() {
        resizeWidth()
        resizeHeight()
    }


    fun resizeWidth() {
        model.windowResizeWidth()
        config.updateWidth(stage.width)
    }

    fun resizeHeight() {
        model.windowResizeHeight()
        config.updateHeight(stage.height)
    }

    private fun changePos() {
        config.updatePos(stage.x, stage.y)
    }

    override fun start(newStage: Stage) {

        // window name
        Platform.setImplicitExit(false)
        SwingUtilities.invokeLater(this::addAppToTray);
        stage = newStage

        var image: Image = Image(HelloApplication::class.java.getResource(APPLICATION_LOGO).toString())
        stage.icons.add(image);
        stage.title = "todoList-dev"
        stage.setOnHiding { event ->
            changePos()
            config.saveToJson("config.json")
        }

        config.loadFromJson("config.json")
        homeScene = Scene(parentView, config.width - 16, config.height - 39)
//        homeScene = Scene(parentView, SCREEN_WIDTH, SCREEN_HEIGHT)

        stage.minWidth = SCREEN_WIDTH
        stage.minHeight = SCREEN_HEIGHT
        if (config.posx != -10000000.0) {
            stage.x = config.posx
            stage.y = config.posy
        }
        stage.widthProperty().addListener {
                _, _, _ -> resizeWidth()
        }
        stage.heightProperty().addListener {
                _, _, _ -> resizeHeight()
        }
        stage.maximizedProperty().addListener {
                _, _, _: Boolean -> Platform.runLater { resizeBoth() }
        }
        homeScene.setOnKeyPressed{e->parentView.keyDispatch(e.code)}
        homeScene.setOnKeyReleased { e->parentView.keyReleased(e.code) }
        stage.scene = homeScene
        showStage()
    }

    private fun showStage() {
        stage.show()
        stage.toFront()
    }

    private fun addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            Toolkit.getDefaultToolkit()

            // app requires system tray support, just exit if there is no support.
            if (!SystemTray.isSupported()) {
//                println("No system tray support, application exiting.")
                Platform.exit()
            }

            // set up a system tray icon.
            val tray = SystemTray.getSystemTray()
            val imageLoc = URL(
                iconImageLoc
            )

            val image: java.awt.image.BufferedImage = ImageIO.read(HelloApplication::class.java.getResource(
                APPLICATION_LOGO))
//            val image: Image = ImageIO.read(imageLoc)
            val trayIcon = TrayIcon(image)

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener { event: ActionEvent? ->
                Platform.runLater(
                    this::showStage
                )
            }

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            val openItem = MenuItem("ToDoList")
            openItem.addActionListener { event: ActionEvent? ->
                Platform.runLater(
                    this::showStage
                )
            }

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            val defaultFont = Font.decode(null)
            val boldFont = defaultFont.deriveFont(Font.BOLD)
            openItem.font = boldFont

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            val exitItem = MenuItem("Exit")
            exitItem.addActionListener { event: ActionEvent? ->
                notificationTimer.cancel()
                Platform.exit()
                tray.remove(trayIcon)
            }

            // setup the popup menu for the application.
            val popup = PopupMenu()
            popup.add(openItem)
            popup.addSeparator()
            popup.add(exitItem)
            trayIcon.popupMenu = popup

            // create a timer which periodically displays a notification message.
            notificationTimer.schedule(
                object : TimerTask() {
                    override fun run() {
                        model.reminderNotify(trayIcon)
                    }
                },
                timeBetweenSequentialNotifications,
                timeToNotificationCheck
            )

            // add the application tray icon to the system tray.
            tray.add(trayIcon)
        } catch (e: AWTException) {
//            println("Unable to init system tray")
//            e.printStackTrace()
        } catch (e: IOException) {
//            println("Unable to init system tray")
//            e.printStackTrace()
        }
    }

    // This will always be called when the application closes
    override fun stop() {
        model.stop()
        super.stop()
    }

}

fun main() {
    Application.launch(HelloApplication::class.java)
}