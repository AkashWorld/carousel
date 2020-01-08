package com.carousal.client.models

import com.carousal.client.models.observables.MediaAction
import com.carousal.client.models.observables.MediaActionObservable
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import tornadofx.runLater

interface MediaActionModel {
    fun setPauseAction(error: () -> Unit)
    fun setPlayAction(error: () -> Unit)
    fun setSeekAction(msTime: Float, error: () -> Unit)
    fun loadMediaAction(filename: String, success: () -> Unit, error: () -> Unit)
    fun subscribeToActions(error: () -> Unit)
    fun getMediaActionObservable(): MediaActionObservable
}

class MediaActionModelImpl : MediaActionModel {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val clientContext = ClientContextImpl.getInstance()
    private val mediaActionObservable =
        MediaActionObservable(null)

    override fun getMediaActionObservable(): MediaActionObservable {
        return mediaActionObservable
    }

    override fun setPauseAction(error: () -> Unit) {
        val mediaPauseMutation = """
            mutation MediaPause {
                pause
            }
        """.trimIndent()
        clientContext.sendQueryOrMutationRequest(mediaPauseMutation, mapOf(), {}, { runLater(error) })
    }

    override fun setPlayAction(error: () -> Unit) {
        val mediaPlayMutation = """
            mutation MediaPlay {
                play
            }
        """.trimIndent()
        clientContext.sendQueryOrMutationRequest(mediaPlayMutation, mapOf(), {}, { runLater(error) })
    }

    override fun setSeekAction(msTime: Float, error: () -> Unit) {
        val mediaSeekMutation = """
            mutation MediaSeek(${"$"}currentTime: Float!) {
                seek(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to msTime.toString())
        clientContext.sendQueryOrMutationRequest(mediaSeekMutation, variables, {}, {
            runLater(error)
        })
    }

    override fun loadMediaAction(filename: String, success: () -> Unit, error: () -> Unit) {
        val loadMediaMutation = """
            mutation MediaLoad(${"$"}file: String!) {
                load(file:${"$"}file) {
                    id
                }
            }
        """.trimIndent()
        val variables = mapOf("file" to filename)
        clientContext.sendQueryOrMutationRequest(loadMediaMutation, variables, { runLater(success) }, {
            runLater(error)
        })
    }

    override fun subscribeToActions(error: () -> Unit) {
        val mediaSubscription = """
            subscription {
                mediaActions {
                    action
                    currentTime
                    user
                }
            }
        """.trimIndent()
        val variables = mapOf<String, Any>()
        clientContext.sendSubscriptionRequest(mediaSubscription, variables, {
            val gson = Gson()
            try {
                val actionJson = gson.fromJson(it, JsonObject::class.java).get("mediaActions")
                val action = gson.fromJson(actionJson, MediaAction::class.java)
                runLater {
                    action?.run { mediaActionObservable.setValue(this) }
                }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
            }
        }, error)
    }
}

