package com.carousal.server.model

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Instant
import kotlin.random.Random

interface UserAuthentication {
    fun verifyUser(token: String?): User?

    fun generateAuthToken(user: User): String?
}

class UserAuthenticationImpl(private val usersRepository: UsersRepository) : UserAuthentication {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val secret: String = (Instant.now().nano * Random(Instant.now().nano).nextDouble()).toString()
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    override fun verifyUser(token: String?): User? {
        if (token == null) return null
        val verifier = JWT.require(algorithm).withIssuer(issuer).build()
        var username: String? = null
        try {
            val jwt = verifier.verify(token)
            val usernameClaim = jwt.getHeaderClaim(usernameClaimHeader)
            if (usernameClaim.isNull) {
                return null
            }
            username = usernameClaim.asString()
        } catch (e: Exception) {
            logger.error(e.message, e.cause)
        }
        return username?.run { usersRepository.getUser(this) }
    }

    override fun generateAuthToken(user: User): String? {
        return try {
            val headerClaims = mapOf(usernameClaimHeader to user.username)
            JWT.create().withIssuer(issuer).withHeader(headerClaims).sign(algorithm)
        } catch (e: JWTCreationException) {
            logger.error(e.message, e.cause)
            null
        }
    }

    companion object {
        private const val usernameClaimHeader = "username"
        private const val issuer = "Carousal Server"
    }
}
