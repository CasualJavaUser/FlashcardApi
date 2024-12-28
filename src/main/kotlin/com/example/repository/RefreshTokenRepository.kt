package com.example.repository

object RefreshTokenRepository {
    private val tokens = mutableMapOf<String, Long>()

    fun findIdByToken(token: String): Long? = tokens[token]

    fun saveToken(token: String, id: Long) {
        tokens[token] = id
    }
}