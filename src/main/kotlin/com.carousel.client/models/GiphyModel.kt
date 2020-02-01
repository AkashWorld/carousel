package com.carousel.client.models

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import tornadofx.runLater
import tornadofx.toObservable
import java.lang.reflect.Type

data class ImageDataPayload(val url: String, val width: Int, val height: Int)

data class GetGiphyRandomId(val getGiphyRandomId: String)
data class GetGiphyTrendingResults(val getGiphyTrendingResults: List<ImageDataPayload>)
data class GetGiphySearchResults(val getGiphySearchResults: List<ImageDataPayload>)

data class DataPayload<T>(val data: T)

class GiphyModel {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var randomId: String? = null
    var currentQuery: String? = null
    val activeList = listOf<ImageDataPayload>().toObservable()

    fun registerRandomIdCallback(success: () -> Unit, error: () -> Unit) {
        val query = """
            query RandomId {
                getGiphyRandomId 
            }
        """.trimIndent()
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(query,
            emptyMap(), {
                val payloadType: Type = object : TypeToken<DataPayload<GetGiphyRandomId>?>() {}.type
                try {
                    val payload: DataPayload<GetGiphyRandomId> = Gson().fromJson(it, payloadType)
                    logger.info("Client received payload: $payload")
                    randomId = payload.data.getGiphyRandomId
                    runLater(success)
                } catch (e: Exception) {
                    logger.error(e.message)
                    runLater(error)
                }
            }, {
                runLater(error)
            })
    }

    fun retrieveGifURLsSearchResults(query: String, error: () -> Unit) {
        if (randomId == null) {
            return
        }
        val gqlQuery = """
            query SearchResults(${"$"}query: String!, ${"$"}randomId: String!, ${"$"}offset: Int!) {
                getGiphySearchResults(query: ${"$"}query, randomId: ${"$"}randomId, offset: ${"$"}offset) {
                    url
                    height
                    width
                }
            }
        """.trimIndent()
        var offset = 0
        if (query == currentQuery) {
            offset = this.activeList.size
        }
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(gqlQuery,
            mapOf("query" to query, "randomId" to randomId!!, "offset" to offset.toString()), {
                val payloadType: Type = object : TypeToken<DataPayload<GetGiphySearchResults?>>() {}.type
                try {
                    val payload: DataPayload<GetGiphySearchResults> = Gson().fromJson(it, payloadType)
                    logger.info("Client received payload: $payload")
                    runLater {
                        if (query != this.currentQuery) {
                            activeList.clear()
                            currentQuery = query
                        }
                        activeList.addAll(payload.data.getGiphySearchResults)
                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                    runLater(error)
                }
            }, {
                runLater(error)
            })
    }

    fun retrieveGifURLsTrendingResults(error: () -> Unit) {
        if (randomId == null) {
            return
        }
        val query = """
            query TrendingResults(${"$"}randomId: String!, ${"$"}offset: Int!) {
                getGiphyTrendingResults(randomId: ${"$"}randomId, offset: ${"$"}offset) {
                    url
                    height
                    width
                }
            }
        """.trimIndent()
        var offset = 0
        if (currentQuery == null) {
            offset = activeList.size
        }
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(query,
            mapOf("randomId" to randomId!!, "offset" to offset.toString()), {
                val payloadType: Type = object : TypeToken<DataPayload<GetGiphyTrendingResults?>>() {}.type
                try {
                    val payload: DataPayload<GetGiphyTrendingResults> = Gson().fromJson(it, payloadType)
                    logger.info("Client received payload: $payload")
                    runLater {
                        if (currentQuery != null) {
                            activeList.clear()
                            currentQuery = null
                        }
                        activeList.addAll(payload.data.getGiphyTrendingResults)
                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                    runLater(error)
                }
            }, {
                runLater(error)
            })
    }
}