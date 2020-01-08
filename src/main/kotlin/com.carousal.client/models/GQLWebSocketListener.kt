package client.models

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

private data class SuccessErrorHandler(val success: (String) -> Unit, val error: () -> Unit)

class GQLWebSocketListener : WebSocketListener() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val handlers = ConcurrentLinkedDeque<SuccessErrorHandler>()
    private val uniqueIdentifiers = ConcurrentHashMap.newKeySet<String>()

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.error("WebSocket Failure", t)
        handlers.parallelStream().forEach { it.error() }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        logger.info(reason)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.info(text)
        handlers.parallelStream().forEach { it.success(text) }
    }

    fun addWebSocketHandler(identifier: String, success: (String) -> Unit, error: () -> Unit): Boolean {
        if(uniqueIdentifiers.contains(identifier)) {
            return false
        }
        uniqueIdentifiers.add(identifier)
        handlers.add(SuccessErrorHandler(success, error))
        return true
    }
}