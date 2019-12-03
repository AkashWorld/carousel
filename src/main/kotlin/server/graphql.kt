package server

import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import server.datafetchers.UserDataFetchers
import server.model.UsersRepository
import java.io.File


const val GRAPHQL_SCHEMA_FILE = "graphql/schema.sdl"

class GraphQLProvider(
    usersRepository: UsersRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private var graphql: GraphQL? = null
    private val userDataFetchers = UserDataFetchers(usersRepository)

    init {
        val schema = this.getSchema()
        if (schema != null) {
            this.graphql = GraphQL.newGraphQL(schema).build()
        } else {
            logger.error("Error initializing GraphQL")
        }
    }

    fun getGraphQL(): GraphQL? {
        return this.graphql
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

