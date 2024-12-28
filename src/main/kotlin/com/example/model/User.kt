package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = -1,
    val login: String,
    val email: String,
    val password: String
)