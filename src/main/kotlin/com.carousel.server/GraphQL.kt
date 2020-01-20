package com.carousel.server

import com.google.gson.Gson
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.SubscriptionExecutionStrategy
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.javalin.websocket.WsMessageContext
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import com.carousel.server.datafetchers.ChatDataFetchers
import com.carousel.server.datafetchers.MediaDataFetchers
import com.carousel.server.datafetchers.NotificationDataFetchers
import com.carousel.server.datafetchers.UserDataFetchers
import com.carousel.server.model.ChatRepository
import com.carousel.server.model.User
import com.carousel.server.model.UserAuthentication
import com.carousel.server.model.UsersRepository
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference


const val GRAPHQL_SCHEMA_FILE = "graphql/schema.sdl"

data class GraphQLQuery(val query: String, val operationName: String?, val variables: Map<String, Any>?)

class GraphQLProvider(
    usersRepository: UsersRepository,
    chatRepository: ChatRepository,
    userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var graphql: GraphQL? = null
    private val userDataFetchers = UserDataFetchers(usersRepository, userAuthentication)
    private val mediaDataFetchers = MediaDataFetchers(usersRepository)
    private val chatFeedDataFetchers = ChatDataFetchers(usersRepository, chatRepository)
    private val notificationDataFetchers = NotificationDataFetchers()

    init {
        val schema = this.getSchema()
        if (schema != null) {
            this.graphql =
                GraphQL.newGraphQL(schema)
                    .subscriptionExecutionStrategy(SubscriptionExecutionStrategy())
                    .build()
        } else {
            logger.error("Error initializing GraphQL")
        }
    }

    fun serveGraphQLQueryMutation(query: String, user: User?): Map<String, Any>? {
        if (graphql == null) {
            logger.error("Could not initialize GraphQL")
            return null
        }
        val graphqlContext: GraphQLContext? = if (user != null) GraphQLContext(user) else null
        val body: GraphQLQuery
        try {
            val gson = Gson()
            body = gson.fromJson(query, GraphQLQuery::class.java)
        } catch (e: Exception) {
            logger.error("Could not parse general GraphQL query", e)
            return null
        }
        val builder: ExecutionInput.Builder = ExecutionInput.newExecutionInput()
        builder.context(graphqlContext)
        builder.query(body.query)
        body.operationName?.let { builder.operationName(it) }
        body.variables.takeIf { !it.isNullOrEmpty() }?.let { builder.variables(it) }
        return graphql?.execute(builder)?.toSpecification()
    }

    fun serveGraphQLSubscription(handler: WsMessageContext, user: User?) {
        if (graphql == null) {
            logger.error("Could not initialize GraphQL")
            return
        }
        val graphqlContext = if (user != null) GraphQLContext(user) else null
        val body: GraphQLQuery
        val query = handler.message()
        logger.info(query)
        try {
            val gson = Gson()
            body = gson.fromJson(query, GraphQLQuery::class.java)
        } catch (e: Exception) {
            logger.error("Could not parse subscription query", e)
            return
        }
        val builder: ExecutionInput.Builder = ExecutionInput.newExecutionInput()
        builder.context(graphqlContext)
        builder.query(body.query)
        body.operationName?.let { builder.operationName(it) }
        body.variables.takeIf { !it.isNullOrEmpty() }?.let { builder.variables(it) }
        val executionInput = builder.build()
        val executionResult = graphql?.execute(executionInput)
        executionResult?.getData<Publisher<ExecutionResult>?>()
            ?.subscribe(object : Subscriber<ExecutionResult> {
                private val subscriptionRef: AtomicReference<Subscription> = AtomicReference()
                override fun onComplete() {
                    logger.info("ws://GraphQL onComplete")
                }

                override fun onSubscribe(subscription: Subscription?) {
                    logger.info("ws://GraphQL onSubscribe")
                    subscriptionRef.set(subscription)
                    subscription?.request(Long.MAX_VALUE)
                }

                override fun onNext(item: ExecutionResult?) {
                    val data = item?.getData<Any>()
                    logger.info("ws://subscriber.onNext -> $data")
                    data?.let { handler.send(it) }
                    subscriptionRef.get().request(Long.MAX_VALUE)
                }

                override fun onError(throwable: Throwable?) {
                    logger.error("ws://GraphQL onError", throwable)
                }
            })
    }

    private fun getSchema(): GraphQLSchema? {
        val schemaGenerator = SchemaGenerator()
        val runtimeWiring = this.getRuntimeWiring() ?: return null
        val typedefRegistry = this.getTypeDefinitionRegistry() ?: return null
        return schemaGenerator.makeExecutableSchema(typedefRegistry, runtimeWiring)
    }

    private fun getRuntimeWiring(): RuntimeWiring? {
        val runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        runtimeWiringBuilder.type("Query") { query ->
            query.dataFetcher("getAllUsers", this.userDataFetchers.queryGetAllUsers())
            query.dataFetcher("getLengthOfChatFeed", this.chatFeedDataFetchers.queryGetLengthOfChatFeed())
            query.dataFetcher("getMessagesPaginated", this.chatFeedDataFetchers.queryGetMessagePaginated())
        }
        runtimeWiringBuilder.type("Mutation") { mutation ->
            mutation.dataFetcher("signIn", this.userDataFetchers.mutationSignIn())
            mutation.dataFetcher("signOut", this.userDataFetchers.mutationSignOut())
            mutation.dataFetcher("play", this.mediaDataFetchers.mutationPlay())
            mutation.dataFetcher("pause", this.mediaDataFetchers.mutationPause())
            mutation.dataFetcher("seek", this.mediaDataFetchers.mutationSeek())
            mutation.dataFetcher("load", this.mediaDataFetchers.mutationLoad())
            mutation.dataFetcher("insertImage", this.chatFeedDataFetchers.mutationInsertImage())
            mutation.dataFetcher("insertMessage", this.chatFeedDataFetchers.mutationInsertMessage())
            mutation.dataFetcher("readyCheck", this.userDataFetchers.mutationReadyCheck())
            mutation.dataFetcher("initiateReadyCheck", this.userDataFetchers.mutationInitiateReadyCheck())
        }
        runtimeWiringBuilder.type("Subscription") { subscription ->
            subscription.dataFetcher("mediaActions", this.mediaDataFetchers.subscriptionMedia())
            subscription.dataFetcher("chatFeed", this.chatFeedDataFetchers.subscriptionChatFeed())
            subscription.dataFetcher("userAction", this.userDataFetchers.subscriptionUserAction())
            subscription.dataFetcher("notification", this.notificationDataFetchers.subscriptionNotification())
        }
        return runtimeWiringBuilder.build()
    }

    private fun getTypeDefinitionRegistry(): TypeDefinitionRegistry? {
        val schemaParser = SchemaParser()
        return try {
            return this::class.java.classLoader.getResourceAsStream(GRAPHQL_SCHEMA_FILE)
                ?.let { InputStreamReader(it) }
                ?.let { schemaParser.parse(it) }
        } catch (e: Exception) {
            logger.error(e.message)
            null
        }
    }

}


