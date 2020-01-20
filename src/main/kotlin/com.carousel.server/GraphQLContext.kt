package com.carousel.server

import com.carousel.server.model.User

data class GraphQLContext constructor(val user: User)