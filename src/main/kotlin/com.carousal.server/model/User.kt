package server.model

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class User(val username: String, var isReady: Boolean, var media: Media?)

class UsersRepository {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
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

    fun clear() {
        usersMap.clear()
    }
}

