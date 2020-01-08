package com.carousal.server

import com.carousal.server.model.User

data class GraphQLContext constructor(val user: User)