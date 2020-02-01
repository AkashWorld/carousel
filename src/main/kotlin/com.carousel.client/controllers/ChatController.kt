package com.carousel.client.controllers

import com.carousel.client.models.*
import com.carousel.client.models.observables.Action
import com.carousel.client.models.observables.MediaAction
import com.carousel.client.views.playerpage.mediaplayer.getMillisecondsToHHMMSS
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.paint.Color
import tornadofx.*

class ChatController : Controller() {
    private val mediaController: MediaController by inject()
    private val usersController: UsersController by inject()
    private val mediaListener: ChangeListener<MediaAction?>
    private val usersActionListener: ChangeListener<UserActionEvent?>
    private val chatModel = ChatModel()
    private val isChatShown = SimpleBooleanProperty(true)
    private val userColorPicker = UserColorPicker()

    init {
        mediaListener =
            ChangeListener { _: ObservableValue<out MediaAction?>?, _: MediaAction?, newValue: MediaAction? ->
                newValue?.let {
                    val message = when (newValue.action) {
                        Action.PAUSE -> {
                            "paused the video"
                        }
                        Action.PLAY -> {
                            "played the video"
                        }
                        Action.SEEK -> {
                            val duration = getMillisecondsToHHMMSS(newValue.currentTime?.toLong() ?: 0L)
                            "changed the video position to $duration"
                        }
                    }
                    runLater {
                        chatModel.addMessage(Message(newValue.user, message, ContentType.INFO))
                    }
                }
            }
        usersActionListener =
            ChangeListener { _, _, newValue ->
                newValue?.run {
                    val message = when (this.action) {
                        UserAction.SIGN_IN -> {
                            "connected"
                        }
                        UserAction.SIGN_OUT -> {
                            "disconnected"
                        }
                        UserAction.IS_READY -> {
                            if (this.user.isReady) {
                                "is ready"
                            } else {
                                "is not ready"
                            }
                        }
                        UserAction.CHANGE_MEDIA -> {
                            "loaded ${this.user.media?.id ?: "a new video"}"
                        }
                        UserAction.READY_CHECK -> {
                            "${this.user.username} initiated a ready check"
                        }
                    }
                    runLater {
                        message.let { chatModel.addMessage(Message(this.user.username, it, ContentType.INFO)) }
                    }
                }
            }
    }


    fun getMessages(): ObservableList<Message> {
        return chatModel.getChatList()
    }

    fun addMessage(content: String) {
        chatModel.sendInsertMessageRequest(content)
    }

    fun addImage(encodedImage: String, success: () -> Unit, error: () -> Unit) {
        chatModel.sendInsertImageRequest(encodedImage, success, error)
    }

    fun addImageUrl(url: String, success: () -> Unit, error: () -> Unit) {
        chatModel.sendInsertImageUrlRequest(url, success, error)
    }

    fun getImageFromURL(url: String): Image {
        var image = chatModel.getCachedImageFromUrl(url)
        if (image == null) {
            image = Image(url, 350.0, 350.0, true, true, true)
        }
        return image
    }

    fun subscribeToMessages(error: () -> Unit) {
        chatModel.sendGetPaginatedMessagesRequest { runLater(error) }
        chatModel.subscribeToMessages { runLater(error) }
        usersController.subscribeToUsersAction { runLater(error) }
        mediaController.getMediaActionObservable().addListener(mediaListener)
        usersController.getUserActionObservable().addListener(usersActionListener)
    }

    fun cleanUp() {
        mediaController.getMediaActionObservable().removeListener(mediaListener)
        usersController.getUserActionObservable().removeListener(usersActionListener)
        chatModel.clear()
    }

    fun tokenizeMessage(message: String): List<String> {
        val retList = mutableListOf<String>()
        var currToken = ""
        var colonStart = false
        for (element in message) {
            if (element == ':') {
                if (currToken != "" && !colonStart) {
                    retList.add(currToken)
                    currToken = ""
                }
                if (!colonStart) {
                    currToken += element
                    colonStart = true
                } else if (colonStart) {
                    currToken += element
                    retList.add(currToken)
                    currToken = ""
                    colonStart = false
                }
            } else if (element == ' ' && colonStart) {
                colonStart = false
                currToken += element
            } else {
                currToken += element
            }
        }
        if (currToken != "") {
            retList.add(currToken)
        }
        return retList
    }

    fun getColor(username: String): Color {
        return userColorPicker.getColorForUsername(username)
    }

    fun setChatShown(chatShown: Boolean) {
        isChatShown.set(chatShown)
    }

    fun isChatShown(): ObservableBooleanValue {
        return isChatShown
    }

    fun toggleIsInfoShown() {
        chatModel.toggleIsInfoShown()
    }
}

