package server.model

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

data class User(val username: String, var media: Media?, var isReady: Boolean = false)

class UsersRepository {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val usersList: AtomicReference<MutableList<User>> = AtomicReference(ArrayList())
    private val usersSet: AtomicReference<MutableSet<String>> = AtomicReference(HashSet())

    fun addUser(user: User): Boolean {
        if (usersSet.get().contains(user.username)) {
            return false
        }
        usersList.get().add(user)
        usersSet.get().add(user.username)
        logger.info("Added user ${user.username}")
        return true
    }

    fun getAllUsers(): List<User> {
        return usersList.get()
    }

    fun getUser(username: String): User? {
        if (!usersSet.get().contains(username)) {
            return null
        }
        return usersList.get().find { it.username == username }
    }

    fun removeUser(user: User): Boolean {
        if (!usersSet.get().contains(user.username)) {
            return false
        }
        usersList.get().removeIf {
            it.username == user.username
        }
        usersSet.get().remove(user.username)
        logger.info("Removed user ${user.username}")
        return true
    }

    fun clear() {
        usersList.get().clear()
        usersSet.get().clear()
    }
}

