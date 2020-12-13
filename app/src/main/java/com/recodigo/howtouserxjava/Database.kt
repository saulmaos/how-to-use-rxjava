package com.recodigo.howtouserxjava

/**
 * Created by SAUL on 13/12/2020.
 */
class Database {
    fun getUser() = User("hola@recodigo.com", "Saúl")
}

data class User(val email: String, val name: String)