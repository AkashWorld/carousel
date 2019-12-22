package server.model

import org.slf4j.LoggerFactory

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
    private val usersList: MutableList<User> = ArrayList()
    private val usersSet: MutableSet<String> = HashSet()

    fun addUser(user: User): Boolean {
        if(usersSet.contains(user.getUsername())) {
            return false
        }
        usersList.add(user)
        usersSet.add(user.getUsername())
        logger.info("Added user ${user.getUsername()}")
        return true
    }

    fun getAllUsers(): List<User> {
        return usersList
    }

    fun getUser(username: String): User? {
        if(!usersSet.contains(username)) {
            return null
        }
        return usersList.find{it.getUsername() == username}
    }

    fun removeUser(user: User): Boolean {
        if(!usersSet.contains(user.getUsername())) {
            return false
        }
        usersList.removeIf {
            it.getUsername() == user.getUsername()
        }
        usersSet.remove(user.getUsername())
        logger.info("Removed user ${user.getUsername()}")
        return true
    }
}

