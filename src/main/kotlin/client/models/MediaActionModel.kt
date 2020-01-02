package client.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory

interface MediaActionModel {
    fun setPauseAction(error: () -> Unit)
    fun setPlayAction(error: () -> Unit)
    fun setSeekAction(msTime: Float, error: () -> Unit)
    fun subscribeToActions(error: () -> Unit): MediaActionObservable
}

class MediaActionModelImpl : MediaActionModel {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val clientContext = ClientContextImpl.getInstance()
    private val mediaActionObservable = MediaActionObservable(null)

    override fun setPauseAction(error: () -> Unit) {
        val mediaPauseMutation = """
            mutation MediaPause {
                pause
            }
        """.trimIndent()
        clientContext.sendQueryOrMutationRequest(mediaPauseMutation, mapOf(), {}, error)
    }

    override fun setPlayAction(error: () -> Unit) {
        val mediaPlayMutation = """
            mutation MediaPlay {
                play
            }
        """.trimIndent()
        clientContext.sendQueryOrMutationRequest(mediaPlayMutation, mapOf(), {}, error)
    }

    override fun setSeekAction(msTime: Float, error: () -> Unit) {
        val mediaSeekMutation = """
            mutation MediaSeek(${"$"}currentTime: Float!) {
                seek(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to msTime.toString())
        clientContext.sendQueryOrMutationRequest(mediaSeekMutation, variables, {}, error)
    }

    override fun subscribeToActions(error: () -> Unit): MediaActionObservable {
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
                mediaActionObservable.setValue(action)
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                error()
            }
        }, error)
        return mediaActionObservable
    }
}

