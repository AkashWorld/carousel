package server.model

interface UserAuthentication {
    fun verifyUser(token: String): Boolean

    fun generateAuthToken(user: User): String
}

class UserAuthenticationImpl: UserAuthentication {
    override fun verifyUser(token: String): Boolean {
        return true
    }

    override fun generateAuthToken(user: User): String {
        return user.getUsername()
    }
}
