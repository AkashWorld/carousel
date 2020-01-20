package com.carousel.server.model

import java.util.concurrent.ConcurrentHashMap

class UsersRepository {
    private val usersMap = ConcurrentHashMap<String, User>()

    fun addUser(user: User): Boolean {
        if (usersMap.contains(user.username)) {
            return false
        }
        usersMap[user.username] = user
        return true
    }

    fun getAllUsers(): List<User> {
        return usersMap.values.toList()
    }

    fun getUser(username: String): User? {
        return usersMap[username]
    }

    fun removeUser(user: User) {
        usersMap.remove(user.username)
    }

    fun isEveryoneReady(): Boolean {
        return !usersMap.values.any { !it.isReady }
    }

    fun clear() {
        usersMap.clear()
    }
}

