package server.model

import org.slf4j.LoggerFactory

interface UserAuthentication {
    fun verifyUser(token: String?): User?

    fun generateAuthToken(user: User): String
}

class UserAuthenticationImpl(private val usersRepository: UsersRepository): UserAuthentication {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    override fun verifyUser(token: String?): User? {
        if(token == null) return null
        return usersRepository.getUser(token)
    }

    override fun generateAuthToken(user: User): String {
        return user.getUsername()
    }
}
