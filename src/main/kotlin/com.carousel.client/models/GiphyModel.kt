package com.carousel.client.models

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import javafx.scene.image.Image
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
    private val imageCache: Cache<String?, List<Pair<String, Image>>> =
        CacheBuilder.newBuilder().maximumSize(100).build<String, List<Pair<String, Image>>>()
    private var trendingCache: List<Pair<String, Image>>? = null
    private var currentQuery: String? = null
    val activeList = listOf<Pair<String, Image>>().toObservable()


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
        if (imageCache.getIfPresent(query) != null && query != currentQuery) {
            currentQuery = query
            activeList.clear()
            activeList.addAll(imageCache.getIfPresent(query)!!)
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
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(gqlQuery,
            mapOf("query" to query, "randomId" to randomId!!, "offset" to 0.toString()), {
                val payloadType: Type = object : TypeToken<DataPayload<GetGiphySearchResults?>>() {}.type
                try {
                    val payload: DataPayload<GetGiphySearchResults> = Gson().fromJson(it, payloadType)
                    logger.info("Client received payload: $payload")
                    runLater {
                        if (query != this.currentQuery) {
                            activeList.clear()
                            currentQuery = query
                        }
                        val images = payload.data.getGiphySearchResults.map { data ->
                            Pair(
                                data.url, Image(
                                    data.url, 235.0, 235.0, true, true, true
                                )
                            )
                        }
                        activeList.addAll(images)
                        imageCache.put(query, images)
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
        if (currentQuery != null && trendingCache != null) {
            currentQuery = null
            activeList.clear()
            activeList.addAll(trendingCache!!)
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
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(query,
            mapOf("randomId" to randomId!!, "offset" to 0.toString()), {
                val payloadType: Type = object : TypeToken<DataPayload<GetGiphyTrendingResults?>>() {}.type
                try {
                    val payload: DataPayload<GetGiphyTrendingResults> = Gson().fromJson(it, payloadType)
                    logger.info("Client received payload: $payload")
                    runLater {
                        val images = payload.data.getGiphyTrendingResults.map { data ->
                            Pair(
                                data.url, Image(
                                    data.url, 235.0, 235.0, true, true, true
                                )
                            )
                        }
                        activeList.clear()
                        currentQuery = null
                        activeList.addAll(images)
                        trendingCache = images
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