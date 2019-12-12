package server

import server.model.User

data class GraphQLContext constructor(val user: User)