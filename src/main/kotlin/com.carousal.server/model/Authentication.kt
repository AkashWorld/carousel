package com.carousal.server.model

interface UserAuthentication {
    fun verifyUser(token: String?): User?

    fun generateAuthToken(user: User): String
}

class UserAuthenticationImpl(private val usersRepository: UsersRepository): UserAuthentication {
    override fun verifyUser(token: String?): User? {
        if(token == null) return null
        return usersRepository.getUser(token)
    }

    override fun generateAuthToken(user: User): String {
        return user.username
    }
}
