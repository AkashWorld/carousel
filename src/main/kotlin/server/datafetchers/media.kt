package server.datafetchers

import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.Media

class MediaDataFetchers {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    fun mutationPlay(): DataFetcher<Boolean?> {
        return DataFetcher {environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("Context(${context.user.getUsername()}): Play at time $currentTime")
            return@DataFetcher true
        }
    }

    fun mutationPause(): DataFetcher<Boolean?> {
        return DataFetcher {environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("Context(${context.user.getUsername()}): Pause at time $currentTime")
            return@DataFetcher true
        }
    }
    fun mutationLoad(): DataFetcher<Media?> {
        return DataFetcher{environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val file: String = environment.getArgument("file")
            logger.info("Context(${context.user.getUsername()}): Loading file $file")
            return@DataFetcher Media(file)
        }
    }
}