package server

import graphql.ExecutionInput
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.LoggerFactory
import com.google.gson.Gson
import server.model.UsersRepository
import server.GraphQLProvider as GraphQLProvider

const val DEFAULT_PORT = 57423;

data class GraphQLQuery(val query: String, val operationName: String?, val variables: Map<String, Any>?)

class Server constructor(private val port: Int = DEFAULT_PORT) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val server: Javalin = Javalin.create()
    private var graphQLProvider: GraphQLProvider = GraphQLProvider(usersRepository = UsersRepository())

    init {
        this.server.post("/graphql") {
            this.serveGraphQLRequest(it)
        }
    }

    fun initialize(): Boolean {
        if (this.graphQLProvider.getGraphQL() === null) {
            logger.error("Will not initialize server as GraphQL is unable to be initalized")
            return false;
        }
        try {
            this.server.start(port)
        } catch (e: Exception) {
            logger.error(e.message)
            return false
        }
        return true
    }

    fun port(): Int {
        return this.server.port()
    }

    private fun serveGraphQLRequest(context: Context) {
        logger.info("post /graphql from ip:${context.ip()}")
        logger.info("Request query: ${context.body()}")
        val graphql = this.graphQLProvider.getGraphQL()
        if (graphql == null) {
            logger.error("Could not initialize GraphQL")
            context.status(400)
            context.result("Could not initialize GraphQL")
            return;
        }
        val body: GraphQLQuery
        try {
            val gson = Gson()
            body = gson.fromJson(context.body(), GraphQLQuery::class.java)
        } catch (e: Exception) {
            logger.error(e.message)
            context.status(400)
            context.result("Could not parse query string")
            return
        }
        val builder: ExecutionInput.Builder = ExecutionInput.newExecutionInput()
        builder.query(body.query)
        body.operationName?.let { builder.operationName(it) }
        body.variables.takeIf { !it.isNullOrEmpty() }?.let { builder.variables(it) }
        val result = graphql.execute(builder).toSpecification()
        context.status(200)
        context.json(result)
        return
    }
}
