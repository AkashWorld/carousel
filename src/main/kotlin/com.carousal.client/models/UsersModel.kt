package com.carousal.client.models

import com.carousal.client.models.observables.User
import com.carousal.client.models.observables.UserActionObservable
import com.carousal.client.models.observables.UserObservable
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import tornadofx.*

enum class UserAction {
    SIGN_IN,
    SIGN_OUT,
    CHANGE_MEDIA,
    IS_READY,
    READY_CHECK
}

data class UserActionEvent(val user: User, val action: UserAction)

class UsersModel {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val context = ClientContextImpl.getInstance()
    private val users: ObservableList<UserObservable> = listOf<UserObservable>().toObservable()
    private val userActionObservable = UserActionObservable()

    fun getUsers(): ObservableList<UserObservable> {
        if (users.isEmpty()) {
            sendGetAllUsersRequest { }
        }
        return users
    }

    fun getUserActionObservable(): UserActionObservable {
        return userActionObservable
    }

    private fun sendGetAllUsersRequest(error: () -> Unit) {
        val query = """
            query AllUsers {
                getAllUsers {
                    username
                    media {
                        id
                    }
                    isReady
                }
            }
        """.trimIndent()
        context.sendQueryOrMutationRequest(query, null, {
            val gson = Gson()
            runLater {
                try {
                    val getAllUsers = gson.fromJson(it, JsonObject::class.java).get("getAllUsers")
                    val usersList = gson.fromJson(getAllUsers, List::class.java) as List<User>
                    users.addAll(usersList.map {
                        UserObservable(
                            it
                        )
                    })
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                    runLater(error)
                    return@runLater
                }
            }
        }, {
            logger.error("Error sending getAllUsers query")
            runLater(error)
        })
    }

    /**
     * This is not a ready check, but whether the user is ready. Unfortunate naming of mutation.
     */
    fun sendIsReady(isReady: Boolean, success: (Boolean) -> Unit, error: () -> Unit) {
        val query = """
            mutation IsReady(${"$"}isReady: Boolean!) {
                 readyCheck(isReady: ${"$"}isReady)
            }
        """.trimIndent()
        context.sendQueryOrMutationRequest(query, mapOf("isReady" to isReady), {
            val gson = Gson()
            runLater {
                try {
                    val readyCheck = gson.fromJson(it, JsonObject::class.java).get("data")
                    val resultMap = gson.fromJson(readyCheck, Map::class.java) as Map<String, Boolean>
                    val result = resultMap["readyCheck"]
                    if (result != null) {
                        success(result)
                    } else {
                        error()
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                    error()
                    return@runLater
                }
            }
        },
            {
                logger.error("Error sending ready check mutation")
                error()
            })
    }

    fun sendInitiateReadyCheck(success: (Boolean) -> Unit, error: () -> Unit) {
        val query = """
            mutation {
                 initiateReadyCheck
            }
        """.trimIndent()
        context.sendQueryOrMutationRequest(query, emptyMap(), {}, {
            logger.error("Error initiating ready check")
            error()
        })
    }

    fun subscribeToUserActions(error: () -> Unit) {
        val query = """
            subscription {
                userAction {
                    action
                    user {
                        username
                        media {
                            id
                        }
                        isReady
                    }
                }
            }
        """.trimIndent()
        context.sendSubscriptionRequest(query, null, {
            runLater {
                val gson = Gson()
                try {
                    val userAction = gson.fromJson(it, JsonObject::class.java).get("userAction")
                    val action = gson.fromJson(userAction, UserActionEvent::class.java)
                    action?.run {
                        userActionObservable.setValue(this)
                        when (this.action) {
                            UserAction.SIGN_IN -> {
                                addUser(this.user)
                            }
                            UserAction.SIGN_OUT -> {
                                removeUser(this.user)
                            }
                            UserAction.CHANGE_MEDIA -> {
                                changeMedia(this.user)
                            }
                            UserAction.IS_READY -> {
                                changeIsReady(this.user)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                    return@runLater
                }
            }

        }, {
            logger.error("Error with userAction subscription")
            runLater(error)
        })
    }

    private fun changeMedia(user: User) {
        user.media?.run { users.find { it.value.username == user.username }?.setMedia(this) }
    }

    private fun changeIsReady(user: User) {
        users.find {
            it.value.username == user.username
        }?.setIsReady(user.isReady)
    }

    private fun removeUser(user: User) {
        users.removeIf { it.value.username == user.username }
    }

    private fun addUser(user: User) {
        users.add(UserObservable(user))
    }

    fun clear() {
        users.clear()
    }
}