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

    fun addUser(user: User) {
        this.usersList.add(user)
    }

    fun getAllUsers(): List<User> {
        return this.usersList
    }
}