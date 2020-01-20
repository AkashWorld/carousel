package com.carousel.server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import com.carousel.server.GraphQLContext
import com.carousel.server.model.Notification
import com.carousel.server.model.User
import com.carousel.server.model.UserAuthentication
import com.carousel.server.model.UsersRepository

enum class UserAction {
    SIGN_IN,
    SIGN_OUT,
    CHANGE_MEDIA,
    IS_READY,
    READY_CHECK
}

data class UserActionEvent(private val action: UserAction, private val user: User)

class UserDataFetchers constructor(
    private val usersRepository: UsersRepository,
    private val userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

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
                publishToUserActionSubscriber(newUser, UserAction.SIGN_IN)
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
            publishToUserActionSubscriber(context.user, UserAction.SIGN_OUT)
            logger.info("Context(${context.user.username}): signOut")
            true
        }
    }

    fun mutationReadyCheck(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val isReady: Boolean = environment.getArgument("isReady")
            context.user.isReady = isReady
            publishToUserActionSubscriber(context.user, UserAction.IS_READY)
            if(!isReady) {
                usersRepository.getAllUsers().forEach {
                    it.getNotificationPublisher()?.publishNotification(Notification("${context.user.username} is not ready."))
                }
            }
            else if (usersRepository.isEveryoneReady()) {
                usersRepository.getAllUsers().forEach {
                    it.getNotificationPublisher()?.publishNotification(Notification("Everyone is ready!"))
                }
            }
            isReady
        }
    }

    fun mutationInitiateReadyCheck(): DataFetcher<Boolean?> {
        return DataFetcher {
            val context: GraphQLContext = it.getContext() ?: return@DataFetcher null
            publishToUserActionSubscriber(context.user, UserAction.READY_CHECK)
            true
        }
    }

    fun subscriptionUserAction(): DataFetcher<Publisher<UserActionEvent>> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("subscriptionUserAction: No context found")
                throw Exception("subscriptionUserAction: No context found")
            }
            val userActionPublisher = UserActionPublisher()
            context.user.setUserActionPublisher(userActionPublisher)
            userActionPublisher
        }
    }

    private fun publishToUserActionSubscriber(user: User, action: UserAction) {
        usersRepository.getAllUsers().forEach {
            it.getUserActionPublisher()?.publishUserActionEvent(user, action)
        }
    }
}

class UserActionPublisher : Publisher<UserActionEvent> {
    private var subscriber: Subscriber<in UserActionEvent>? = null

    override fun subscribe(s: Subscriber<in UserActionEvent>?) {
        subscriber = s
    }

    fun publishUserActionEvent(user: User, event: UserAction) {
        subscriber?.onNext(UserActionEvent(event, user))
    }
}
