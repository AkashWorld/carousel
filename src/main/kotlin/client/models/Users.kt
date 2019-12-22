package client.models

data class User(private val userName: String)

class Users {
    private val users: MutableList<User> = mutableListOf()

    init {
        users.add(User("Lone Hunt"))
        users.add(User("Dabessbeast"))
        users.add(User("awildwildboar"))
    }
}