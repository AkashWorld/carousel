package server

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import okhttp3.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface ExternalIPProvider {
    fun getExternalIp(future: CompletableFuture<String>)
}

interface ExternalIPRetriever {
    fun retrieveIP(future: CompletableFuture<String>)
}

private val logger = LoggerFactory.getLogger("ExternalIPProvider")

class ExternalIPProviderImpl : ExternalIPProvider {
    private val client = OkHttpClient()
    private val ipCache: Cache<String, String> =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).build<String, String>()
    private val cacheKey = ExternalIPRetriever::class.java.simpleName


    override fun getExternalIp(future: CompletableFuture<String>) {
        val cachedValue = ipCache.getIfPresent(cacheKey)
        if (cachedValue != null) {
            future.complete(cachedValue)
            return
        }
        val retrievers = listOf(AmazonAWS(client), ICanHazIp(client), MyExternalIP(client), IPInfo(client))
        retrievers.forEach {
            val currFuture = CompletableFuture<String>()
            it.retrieveIP(currFuture)
            try {
                currFuture.get()?.run {
                    if (validateIPV4AddressStructure(this)) {
                        this
                    } else {
                        null
                    }
                }?.run {
                    ipCache.put(cacheKey, this)
                    future.complete(this)
                    return
                }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
            }
        }
        future.completeExceptionally(Exception("Could not retrieve external IP"))
    }

    companion object {
        fun validateIPV4AddressStructure(address: String): Boolean {
            val tokens = address.split(".")
            if (tokens.size != 4) {
                return false
            }
            val result = tokens.filter {
                try {
                    val token = Integer.valueOf(it)
                    token in 0..255
                } catch (e: Exception) {
                    false
                }
            }
            return result.size == 4
        }
    }
}

class AmazonAWS(private val client: OkHttpClient) : ExternalIPRetriever {
    override fun retrieveIP(future: CompletableFuture<String>) {
        val request = Request.Builder().url("http://checkip.amazonaws.com").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                future.completeExceptionally(Exception("Could not retrieve external IP"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    future.completeExceptionally(Exception("Could not retrieve external IP"))
                    return
                }
                val responseBody = response.body?.string()?.substringBeforeLast('\n')
                future.complete(responseBody)
            }
        })
    }
}

class ICanHazIp(private val client: OkHttpClient) : ExternalIPRetriever {
    override fun retrieveIP(future: CompletableFuture<String>) {
        val request = Request.Builder().url("https://ipv4.icanhazip.com").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                future.completeExceptionally(Exception("Could not retrieve external IP"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    future.completeExceptionally(Exception("Could not retrieve external IP"))
                    return
                }
                val responseBody = response.body?.string()?.substringBeforeLast('\n')
                future.complete(responseBody)
            }
        })
    }
}

class MyExternalIP(private val client: OkHttpClient) : ExternalIPRetriever {
    override fun retrieveIP(future: CompletableFuture<String>) {
        val request = Request.Builder().url("http://myexternalip.com/raw").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                future.completeExceptionally(Exception("Could not retrieve external IP"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    future.completeExceptionally(Exception("Could not retrieve external IP"))
                    return
                }
                val responseBody = response.body?.string()?.substringBeforeLast('\n')
                future.complete(responseBody)
            }
        })
    }
}

class IPInfo(private val client: OkHttpClient) : ExternalIPRetriever {
    override fun retrieveIP(future: CompletableFuture<String>) {
        val request = Request.Builder().url("http://ipinfo.io/ip").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                future.completeExceptionally(Exception("myexternalip failed"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    future.completeExceptionally(Exception("myexternalip failed"))
                    return
                }
                val responseBody = response.body?.string()?.substringBeforeLast('\n')
                future.complete(responseBody)
            }
        })
    }
}
