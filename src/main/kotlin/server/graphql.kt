package server

import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.slf4j.LoggerFactory
import java.io.File


const val GRAPHQL_SCHEMA_FILE = "graphql/schema.sdl"

class GraphQLProvider {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private var graphql: GraphQL? = null

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
        val runtimeWiring = this.getRuntimeWiring()
        if (runtimeWiring === null) {
            return null
        }
        val typedefRegistry = this.getTypeDefinitionRegistry()
        if (typedefRegistry === null) {
            return null
        }
        return schemaGenerator.makeExecutableSchema(typedefRegistry, runtimeWiring);
    }

    private fun getRuntimeWiring(): RuntimeWiring? {
        val runtimeWiring = RuntimeWiring.newRuntimeWiring().build();
        return runtimeWiring
    }

    private fun getTypeDefinitionRegistry(): TypeDefinitionRegistry? {
        val schemaParser = SchemaParser()
        val url = this::class.java.classLoader.getResource(GRAPHQL_SCHEMA_FILE);
        if (url === null) {
            logger.error("URL for GraphQL Schema resource not found")
            return null;
        }
        val file = File(url.toURI())
        return try {
            schemaParser.parse(file)
        } catch (e: Exception) {
            logger.error(e.message)
            null;
        }
    }

}

