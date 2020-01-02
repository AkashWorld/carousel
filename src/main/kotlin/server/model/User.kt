package server.model

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

class User constructor(private val username: String) {
    private var media: Media? = null

    fun getUsername(): String {
        return this.username
    }

    fun setMedia(id: String) {
        this.media = Media(id)
    }

    fun getMedia(): Media? {
        return this.media
    }
}

class UsersRepository {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val usersList: AtomicReference<MutableList<User>> = AtomicReference(ArrayList())
    private val usersSet: AtomicReference<MutableSet<String>> = AtomicReference(HashSet())

    fun addUser(user: User): Boolean {
        if (usersSet.get().contains(user.getUsername())) {
            return false
        }
        usersList.get().add(user)
        usersSet.get().add(user.getUsername())
        logger.info("Added user ${user.getUsername()}")
        return true
    }

    fun getAllUsers(): List<User> {
        return usersList.get()
    }

    fun getUser(username: String): User? {
        if (!usersSet.get().contains(username)) {
            return null
        }
        return usersList.get().find { it.getUsername() == username }
    }

    fun removeUser(user: User): Boolean {
        if (!usersSet.get().contains(user.getUsername())) {
            return false
        }
        usersList.get().removeIf {
            it.getUsername() == user.getUsername()
        }
        usersSet.get().remove(user.getUsername())
        logger.info("Removed user ${user.getUsername()}")
        return true
    }

    fun clear() {
        usersList.get().clear()
        usersSet.get().clear()
    }
}

