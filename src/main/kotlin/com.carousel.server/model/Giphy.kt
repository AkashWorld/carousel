package com.carousel.server.model

import com.google.common.cache.CacheBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.lang.reflect.Type

data class DataPayload<T>(val data: T)

/**
 * RandomID response payloads
 */
data class RandomIdPayload(val random_id: String)

/**
 * Search response payloads
 */
data class SearchGifPayload(val type: String, val images: ImagesPayload)

data class ImagesPayload(val downsized_medium: ImageDataPayload)
data class ImageDataPayload(val url: String, val width: String, val height: String, val size: Int)

class Giphy {
    companion object {
        private const val GIPHY_API_KEY = "api_key"
    }

    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val GIPHY_API_VALUE = System.getenv("GIPHY_API_KEY")
    private val client = OkHttpClient()
    private val gson = Gson()
    private val gifSearchUrl = "api.giphy.com/v1/gifs/search"
    private val gifTrendingUrl = "api.giphy.com/v1/gifs/trending"
    private val randomIdUrl = "api.giphy.com/v1/randomid"
    private val queryImageCache = CacheBuilder.newBuilder().maximumSize(500).build<String, List<ImageDataPayload>>()
    private val trendingResults = mutableListOf<ImageDataPayload>()

    fun getGIPHYRandomId(): String? {
        if (GIPHY_API_VALUE == null) {
            return null
        }
        val request: Request = Request.Builder()
            .get()
            .url("https://$randomIdUrl?$GIPHY_API_KEY=$GIPHY_API_VALUE")
            .build()
        val resp = client.newCall(request).execute()
        if (resp.code != 200) {
            return null
        }
        val payloadType: Type = object : TypeToken<DataPayload<RandomIdPayload?>?>() {}.type
        return try {
            val payload: DataPayload<RandomIdPayload> = gson.fromJson(resp.body?.string(), payloadType)
            logger.info(payload.toString())
            payload.data.random_id
        } catch (e: Exception) {
            logger.error(e.message)
            null
        }
    }

    fun getGiphySearchRequest(query: String, randomId: String?, offset: Int = 0): List<ImageDataPayload>? {
        if (GIPHY_API_VALUE == null) {
            return null
        }
        if(queryImageCache.getIfPresent(query) != null) {
            return queryImageCache.getIfPresent(query) as List<ImageDataPayload>
        }
        var urlApi = "https://$gifSearchUrl?$GIPHY_API_KEY=$GIPHY_API_VALUE&q=$query&offset=$offset"
        randomId?.run { urlApi += "&random_id=$this" }
        val request: Request = Request.Builder()
            .get()
            .url(urlApi)
            .build()
        val resp = client.newCall(request).execute()
        if (resp.code != 200) {
            return null
        }
        val payloadType: Type = object : TypeToken<DataPayload<List<SearchGifPayload>?>?>() {}.type
        return try {
            val payload: DataPayload<List<SearchGifPayload>> = gson.fromJson(resp.body?.string(), payloadType)
            logger.info(payload.toString())
            val payloadImageData = transformPayloadUrl(payload.data.map { it.images.downsized_medium })
            queryImageCache.put(query, payloadImageData)
            payloadImageData
        } catch (e: Exception) {
            logger.error(e.message)
            null
        }
    }

    fun getGiphyTrendingRequest(randomId: String?, offset: Int = 0): List<ImageDataPayload>? {
        if (GIPHY_API_VALUE == null) {
            return null
        }
        if(trendingResults.isNotEmpty()) {
            return trendingResults
        }
        var urlApi = "https://$gifTrendingUrl?$GIPHY_API_KEY=$GIPHY_API_VALUE&offset=$offset"
        randomId?.run { urlApi += "&random_id=$this" }
        val request: Request = Request.Builder()
            .get()
            .url(urlApi)
            .build()
        val resp = client.newCall(request).execute()
        if (resp.code != 200) {
            return null
        }
        val payloadType: Type = object : TypeToken<DataPayload<List<SearchGifPayload>?>?>() {}.type
        return try {
            val payload: DataPayload<List<SearchGifPayload>> = gson.fromJson(resp.body?.string(), payloadType)
            logger.info(payload.toString())
            val payloadImageData = transformPayloadUrl(payload.data.map { it.images.downsized_medium })
            trendingResults.addAll(payloadImageData)
            payloadImageData
        } catch (e: Exception) {
            logger.error(e.message)
            null
        }
    }

    private fun transformPayloadUrl(payload: List<ImageDataPayload>): List<ImageDataPayload> {
        return payload.map {
            var newUrl = it.url.replace("media/", "")
            newUrl = newUrl.substringAfter(".")
            newUrl = "https://i.$newUrl"
            newUrl = newUrl.replace("/giphy", "")
            val image = ImageDataPayload(newUrl, it.width, it.height, it.size)
            image
        }
    }
}