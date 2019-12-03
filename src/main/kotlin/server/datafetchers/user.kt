package server.datafetchers

import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import server.model.User
import server.model.UsersRepository

class UserDataFetchers constructor(private val usersRepository: UsersRepository) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);

    fun queryGetAllUsers(): DataFetcher<List<User>> {
        return DataFetcher {
            //TODO: Auth
            this.usersRepository.getAllUsers()
        }
    }

    fun mutationSignIn(): DataFetcher<String> {
        return DataFetcher { environment ->
            val password: String? = environment.getArgument("password")
            //TODO: Auth
            val username: String = environment.getArgument("username")
            val newUser = User(username)
            this.usersRepository.addUser(newUser)
            username
        }
    }
}
