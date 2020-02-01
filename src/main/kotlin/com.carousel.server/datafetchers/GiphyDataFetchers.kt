package com.carousel.server.datafetchers

import com.carousel.server.GraphQLContext
import com.carousel.server.model.Giphy
import com.carousel.server.model.ImageDataPayload
import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory

class GiphyDataFetchers {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val giphy = Giphy()

    fun queryGetGiphyRandomId(): DataFetcher<String?> {
        return DataFetcher {
            val context = it.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetGiphyRandomId: No context found")
                throw Exception("queryGetGiphyRandomId: No context found")
            }
            giphy.getGIPHYRandomId()
        }
    }

    fun queryGetGiphyTrendingResults(): DataFetcher<List<ImageDataPayload?>?> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetGiphyTrendingResults: No context found")
                throw Exception("queryGetGiphyTrendingResults: No context found")
            }
            val randomId = environment.getArgument<String>("randomId")
            val offset = environment.getArgument<Int>("offset")
            giphy.getGiphyTrendingRequest(randomId, offset)
        }
    }

    fun queryGetGiphySearchResults(): DataFetcher<List<ImageDataPayload?>?> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetGiphySearchResults: No context found")
                throw Exception("queryGetGiphySearchResults: No context found")
            }
            val query = environment.getArgument<String>("query")
            val randomId = environment.getArgument<String>("randomId")
            val offset = environment.getArgument<Int>("offset")
            giphy.getGiphySearchRequest(query, randomId, offset)
        }
    }
}