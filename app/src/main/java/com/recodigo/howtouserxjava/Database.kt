package com.recodigo.howtouserxjava

/**
 * Created by SAUL on 13/12/2020.
 */
class Database {
    fun getUser() = User("hola@recodigo.com", "Saúl")

    fun updateUser(user: User): Boolean {
        val random = (0..10).random()
        return random % 2 == 0
    }

    fun getUser(email: String): User? {
        val random = (0..10).random()
        return if (random % 2 == 0) User(email, "Saúl")
        else null
    }
}

data class User(val email: String, val name: String)