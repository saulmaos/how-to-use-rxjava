package com.recodigo.howtouserxjava

/**
 * Created by SAUL on 13/12/2020.
 */
class ApiService {
    fun getRemoteUser(email: String) = RemoteUser(email, "Saul", "Profile pic url", "México")
}

data class RemoteUser(val email: String, val name: String, val profilePicUrl: String, val country: String)