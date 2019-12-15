package server

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
import server.datafetchers.MediaDataFetchers
import server.datafetchers.UserDataFetchers
import server.model.User
import server.model.UserAuthentication
import server.model.UsersRepository
import java.io.File


const val GRAPHQL_SCHEMA_FILE = "graphql/schema.sdl"

class GraphQLProvider(
    usersRepository: UsersRepository,
    userAuthentication: UserAuthentication
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private var graphql: GraphQL? = null
    private val userDataFetchers = UserDataFetchers(usersRepository, userAuthentication)
    private val mediaDataFetchers = MediaDataFetchers()

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
            return null;
        }
        val graphqlContext: GraphQLContext? = if (user != null) GraphQLContext(user) else null
        val body: GraphQLQuery
        try {
            val gson = Gson()
            body = gson.fromJson(query, GraphQLQuery::class.java)
        } catch (e: Exception) {
            logger.error("Could not parse general GraphQL query", e)
            return null;
        }
        val builder: ExecutionInput.Builder = ExecutionInput.newExecutionInput()
        builder.context(graphqlContext)
        builder.query(body.query)
        body.operationName?.let { builder.operationName(it) }
        body.variables.takeIf { !it.isNullOrEmpty() }?.let { builder.variables(it) }
        return graphql?.execute(builder)?.toSpecification()
    }

    fun serveGraphQLSubscription(handler: WsMessageContext, query: String, user: User) {
        if (graphql == null) {
            logger.error("Could not initialize GraphQL")
            return
        }
        val graphqlContext: GraphQLContext = GraphQLContext(user)
        val body: GraphQLQuery
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
        val subscriptionStream: Publisher<ExecutionResult>? = executionResult?.getData()
    }

    private fun getSchema(): GraphQLSchema? {
        val schemaGenerator = SchemaGenerator()
        val runtimeWiring = this.getRuntimeWiring() ?: return null
        val typedefRegistry = this.getTypeDefinitionRegistry() ?: return null
        return schemaGenerator.makeExecutableSchema(typedefRegistry, runtimeWiring);
    }

    private fun getRuntimeWiring(): RuntimeWiring? {
        val runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        runtimeWiringBuilder.type("Query") { query ->
            query.dataFetcher("getAllUsers", this.userDataFetchers.queryGetAllUsers())
        }
        runtimeWiringBuilder.type("Mutation") { mutation ->
            mutation.dataFetcher("signIn", this.userDataFetchers.mutationSignIn())
            mutation.dataFetcher("signOut", this.userDataFetchers.mutationSignOut())
            mutation.dataFetcher("play", this.mediaDataFetchers.mutationPlay())
            mutation.dataFetcher("pause", this.mediaDataFetchers.mutationPause())
            mutation.dataFetcher("load", this.mediaDataFetchers.mutationLoad())
        }
        return runtimeWiringBuilder.build()
    }

    private fun getTypeDefinitionRegistry(): TypeDefinitionRegistry? {
        val schemaParser = SchemaParser()
        return try {
            return this::class.java.classLoader.getResource(GRAPHQL_SCHEMA_FILE)
                ?.let { File(it.toURI()) }
                ?.let { schemaParser.parse(it) }
        } catch (e: Exception) {
            logger.error(e.message)
            null;
        }
    }

}

