package com.schedule.demolist.persistence

import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.google.gson.Gson
import com.schedule.demolist.model.MainHandler
import com.schedule.demolist.constructFilePath
import com.schedule.demolist.persistence.schemas.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.lang.Exception

class PersistenceLayer {
    private val CREATE_NEW_USER_ENDPOINT = "https://as8xzhv5y8.execute-api.us-east-1.amazonaws.com/prod"
    private val UPDATE_USER_NOTES_ENDPOINT = "https://3ehh1m9441.execute-api.us-east-1.amazonaws.com/prod"
    private val READ_USER_NOTES_ENDPOINT = "https://09a76x2eo2.execute-api.us-east-1.amazonaws.com/prod"
    private val DELETE_USER_ENDPOINT = "https://rr8n3arfz8.execute-api.us-east-1.amazonaws.com/prod"
    private val CHANGE_USERNAME_ENDPOINT = "https://o7am00uyef.execute-api.us-east-1.amazonaws.com/prod"
    private val CHANGE_PASSWORD_ENDPOINT = "https://gnjds4a0r4.execute-api.us-east-1.amazonaws.com/prod"
    private val USER_LOGIN_ENDPOINT = "https://re7qj4lbc8.execute-api.us-east-1.amazonaws.com/prod"
    private var inSyncMode = false
    private var jwt = ""
    private var curUser = ""
    private var userCache: MutableMap<String, UserStruct> = mutableMapOf<String, UserStruct>()

    init{
        loadUserCacheFromDisk()
    }
    fun loadUserCacheFromDisk(){
        var filePath = constructFilePath("foo.json")
        val file = File(filePath)
        if (!file.exists()) {
            var emptySerial = Json.encodeToString(userCache)
            File(filePath).writeText(emptySerial)
        }
        val usrInputStream: InputStream = file.inputStream()
        var serializedUserCache = usrInputStream.bufferedReader().use { it.readText() }
        userCache = Json.decodeFromString(serializedUserCache)
    }

    fun saveToJson(newState: MainHandler){
        var serializedState = Json.encodeToString(newState)
        if (curUser in userCache){
            userCache[curUser]!!.serializedState = serializedState
        }
        var serializedUserCache = Json.encodeToString(userCache)
        var filePath = constructFilePath("foo.json")
        File(filePath).writeText(serializedUserCache)
    }

    fun onClose(newState: MainHandler){
        saveToJson(newState)
        if (inSyncMode){
            updateUserNotes(newState)
        }
    }

    //Intended Control Flow
    //Run attempt login
    //If False the re-run attempt login with new credentials until a pass
    //Run getStateFromPersistenceLayer to get the state from whichever layer was accessed
    fun attemptLogin(username: String, password: String):Boolean{
        curUser = username
        var logonStatus = loginToServer(username, password)
        if (logonStatus == 200){
            inSyncMode = true
            if (username !in userCache){
                userCache[username] = UserStruct(username, password, "{}", false)
            }
            return true //need to update the cache
        } else if (logonStatus == 401){ //incorrect password
            return false
        } else{
            if (username in userCache && userCache[username]!!.password == password){
                inSyncMode = false
                userCache[username]!!.dirtyHuh = true
                return true
            }
            return false
        }
    }

    fun getStateFromPersistenceLayer(): MainHandler?{
        if (inSyncMode){
            if (!userCache[curUser]!!.dirtyHuh){
                var toLoad = fetchTasksFromServer()
                userCache[curUser]!!.serializedState = Json.encodeToString(toLoad)
                return toLoad
            } else{
                var serializedState = userCache[curUser]!!.serializedState
                userCache[curUser]!!.dirtyHuh = false
                var unSerial = Json.decodeFromString<MainHandler>(serializedState)
                updateUserNotes(unSerial)
                return unSerial
            }
        }
        var serializedState = userCache[curUser]!!.serializedState
        return Json.decodeFromString(serializedState)
    }

    ///////////////////////////////////API CALLS
    fun createNewUser(username:String, password:String):Boolean{
        var hitUrl = CREATE_NEW_USER_ENDPOINT
        curUser = username
        var defaultHandler = Json.encodeToString(MainHandler())
        var jPayload = NewUserRequestStruct(username, password,defaultHandler)
        val (_, resp, result) = hitUrl
            .httpPut().jsonBody(jPayload)
            .responseString()
        try{
            inSyncMode = true
            var jResult = Gson().fromJson(result.component1(), LoginResponse::class.java)
            if (jResult.statusCode != 200){return false}
            jwt = jResult.token
            userCache[username] = UserStruct(username, password,jResult.body, false)
            return true
        } catch(e:Exception){
            return false
        }
    }
    fun loginToServer(username: String, password: String):Int{
        var hitUrl = USER_LOGIN_ENDPOINT
        var jPayload = LoginStruct(username, password)
        val (_, resp, result) = hitUrl
            .httpPost().jsonBody(jPayload)
            .responseString()
        try{
            var jResult = Gson().fromJson(result.component1(), LoginResponse::class.java)
            if (jResult.statusCode != 200){return jResult.statusCode}
            jwt = jResult.token
            return jResult.statusCode
        } catch (ex: Exception){
            return 400
        }
    }
    fun fetchTasksFromServer(): MainHandler? {
        if (jwt == "") {
            return null
        }
        var hitUrl = READ_USER_NOTES_ENDPOINT
        var tokenPayload = TokenStruct(jwt)
        val (_, resp, result) = hitUrl
            .httpGet(listOf(Pair<String, String>("token", jwt)))
            .jsonBody(tokenPayload)
            .responseString()
        try{
            var jResult = Gson().fromJson(result.component1(), UpdateResponse::class.java)
            return Json.decodeFromString<MainHandler>(jResult.body)
        } catch (e:Exception){
            return MainHandler()
        }

    }
    fun updateUserNotes(state: MainHandler){
        if (jwt == "") {return}
        var hitUrl = UPDATE_USER_NOTES_ENDPOINT
        var tokenPayload = UpdateStruct(jwt, Json.encodeToString(state))
        val (_, resp, _) = hitUrl
            .httpPost()
            .jsonBody(tokenPayload)
            .responseString()
    }
    fun updateUserPassword(newPassword: String): Boolean{
        if (jwt == "") {return false}
        var hitUrl = CHANGE_PASSWORD_ENDPOINT
        var payload = ChangeUserPasswordRequest(jwt, newPassword)
        val (_, resp, result) = hitUrl
            .httpPost()
            .jsonBody(payload)
            .responseString()
        try{
            var jResult = Gson().fromJson(result.component1(), UpdateResponse::class.java)
            if (jResult.statusCode != 200){return false}
            userCache[curUser]!!.password = newPassword
            return true
        } catch(e: Exception){
            return false
        }
    }
    fun updateUserUsername(newUsername: String):Boolean{
        if (jwt == "") {return false}
        var hitUrl = CHANGE_USERNAME_ENDPOINT
        var payload = ChangeUserUsernameRequest(jwt, newUsername)
        val (_, resp, result) = hitUrl
            .httpPost()
            .jsonBody(payload)
            .responseString()
        try{
            var jResult = Gson().fromJson(result.component1(), LoginResponse::class.java)
            if (jResult.statusCode != 200){return false}
            jwt = jResult.token
            var newUser = UserStruct(newUsername, userCache[curUser]!!.password,
                                    userCache[curUser]!!.serializedState, userCache[curUser]!!.dirtyHuh)
            userCache.remove(curUser)
            curUser = newUsername
            userCache[newUsername] = newUser
            return true
        } catch(e: Exception){
            return false
        }
    }
}