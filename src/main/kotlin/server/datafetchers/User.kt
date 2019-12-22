package server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.User
import server.model.UserAuthentication
import server.model.UsersRepository
import java.util.concurrent.atomic.AtomicReference

enum class UserAction {
    SIGN_IN,
    SIGN_OUT,
    CHANGE_MEDIA
}

data class UserActionEvent(private val action: UserAction, private val user: User)

class UserDataFetchers constructor(
    private val usersRepository: UsersRepository,
    private val userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val userActionPublisher = UserActionPublisher()

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
                this.userActionPublisher.publishUserActionEvent(newUser, UserAction.SIGN_IN)
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
            this.userActionPublisher.publishUserActionEvent(context.user, UserAction.SIGN_OUT)
            logger.info("Context(${context.user.getUsername()}): signOut")
            true
        }
    }

    fun subscriptionUserAction(): DataFetcher<Publisher<UserActionEvent>> {
        return DataFetcher {environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("queryGetMessagePaginated: No context found")
            }
            userActionPublisher
        }
    }
}

class UserActionPublisher: Publisher<UserActionEvent> {
    private val s: AtomicReference<Subscriber<in UserActionEvent>?> = AtomicReference()

    override fun subscribe(s: Subscriber<in UserActionEvent>?) {
        this.s.set(s)
    }

    fun publishUserActionEvent(user: User, event: UserAction) {
        this.s.get()?.onNext(UserActionEvent(event, user))
    }
}
