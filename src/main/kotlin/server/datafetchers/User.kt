package server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.User
import server.model.UserAuthentication
import server.model.UsersRepository
import java.util.concurrent.ConcurrentLinkedQueue

enum class UserAction {
    SIGN_IN,
    SIGN_OUT,
    CHANGE_MEDIA,
    IS_READY
}

data class UserActionEvent(private val action: UserAction, private val user: User)

class UserDataFetchers constructor(
    private val usersRepository: UsersRepository,
    private val userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val userActionPublisher = UserActionPublisher()

    fun queryGetAllUsers(): DataFetcher<List<User>?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("Context(${context.user.username}): getAllUsers")
            this.usersRepository.getAllUsers()
        }
    }

    fun mutationSignIn(): DataFetcher<String?> {
        return DataFetcher { environment ->
            val password: String? = environment.getArgument("password")
            val username: String = environment.getArgument("username")
            val newUser = User(username, false, null)
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
            logger.info("Context(${context.user.username}): signOut")
            true
        }
    }

    fun mutationReadyCheck(): DataFetcher<Boolean?> {
        return DataFetcher {
            val context: GraphQLContext = it.getContext() ?: return@DataFetcher null
            val isReady: Boolean = it.getArgument("isReady")
            context.user.isReady = isReady
            this.userActionPublisher.publishUserActionEvent(context.user, UserAction.IS_READY)
            isReady
        }
    }

    fun subscriptionUserAction(): DataFetcher<Publisher<UserActionEvent>> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("subscriptionUserAction: No context found")
                throw Exception("subscriptionUserAction: No context found")
            }
            userActionPublisher
        }
    }

    fun getUserActionPublisher(): UserActionPublisher {
        return userActionPublisher
    }
}

class UserActionPublisher : Publisher<UserActionEvent> {
    private val subscribers: ConcurrentLinkedQueue<Subscriber<in UserActionEvent>?> =
        ConcurrentLinkedQueue()

    override fun subscribe(s: Subscriber<in UserActionEvent>?) {
        subscribers.add(s)
    }

    fun publishUserActionEvent(user: User, event: UserAction) {
        subscribers.forEach {
            it?.onNext(UserActionEvent(event, user))
        }
    }
}
