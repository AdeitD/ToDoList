package com.schedule.demolist.persistence.schemas
import kotlinx.serialization.Serializable

@Serializable
class LoginStruct {
    public val username: String
    public val details: String
    constructor(pusername: String, ppassword: String){
        username=pusername
        details = ppassword
    }
}
@Serializable
data class UpdateResponse (
    val statusCode: Int,
    val body: String,
    val headers: Headers
)

@Serializable
class TokenStruct {
    val token: String
    constructor(ttoken: String){
        token=ttoken
    }
}

@Serializable
class UpdateStruct {
    val token: String
    val newUserInfo: String
    constructor(ttoken: String, state:String){
        token=ttoken
        newUserInfo = state
    }
}
@Serializable
data class UserStruct (
    val username: String,
    var password: String,
    var serializedState: String,
    var dirtyHuh: Boolean
)
@Serializable
data class ChangeUserPasswordRequest (
    val token: String,
    var newUserDetails: String,
)
@Serializable
data class ChangeUserUsernameRequest (
    val token: String,
    var newUserName: String,
)
@Serializable
data class NewUserRequestStruct (
    val username: String,
    val details: String,
    var serializedState: String,
)
data class LoginResponse (
    val statusCode: Int,
    val body: String,
    val token: String,
    val headers: Headers
)
@Serializable
data class Headers (
    val contentType: String,
    val accessControlAllowOrigin: String
)