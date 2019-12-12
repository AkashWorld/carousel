package server.datafetchers

import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.User
import server.model.UserAuthentication
import server.model.UserAuthenticationImpl
import server.model.UsersRepository

class UserDataFetchers constructor(
    private val usersRepository: UsersRepository,
    private val userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);

    fun queryGetAllUsers(): DataFetcher<List<User>?> {
        return DataFetcher {environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("Context(${context.user.getUsername()}): getAllUsers")
            this.usersRepository.getAllUsers()
        }
    }

    fun mutationSignIn(): DataFetcher<String?> {
        return DataFetcher { environment ->
            val password: String? = environment.getArgument("password")
            val username: String = environment.getArgument("username")
            val newUser = User(username)
            if (usersRepository.addUser(newUser)) {
                userAuthentication.generateAuthToken(newUser)
            } else {
                logger.error("Could not add username $username")
                null
            }
        }
    }

    fun mutationSignOut(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            usersRepository.removeUser(context.user)
            logger.info("Context(${context.user.getUsername()}): signOut")
            true
        }
    }
}
