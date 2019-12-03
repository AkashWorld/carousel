package server.model

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
    private val usersList: MutableList<User> = ArrayList();
    private val usersSet: MutableSet<User> = HashSet()

    fun addUser(user: User): Boolean {
        if(usersSet.contains(user)) {
            return false
        }
        usersList.add(user)
        usersSet.add(user)
        return true
    }

    fun getAllUsers(): List<User> {
        return usersList
    }
}

