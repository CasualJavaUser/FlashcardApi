package com.example.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.model.User
import com.example.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.*

class JwtService(application: Application) {
    private val secret = application.environment.config.property("jwt.secret").getString()
    private val issuer = application.environment.config.property("jwt.issuer").getString()
    private val audience = application.environment.config.property("jwt.audience").getString()
    val realm = application.environment.config.property("jwt.realm").getString()

    val jwtVerifier: JWTVerifier =
        JWT
            .require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

    fun createAccessToken(id: Long): String =
        createJwtToken(id, 60000)

    fun createRefreshToken(id: Long): String =
        createJwtToken(id, 86400000)

    private fun createJwtToken(id: Long, expireIn: Int): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", id)
            .withExpiresAt(Date(System.currentTimeMillis() + expireIn))
            .sign(Algorithm.HMAC256(secret))

    fun validate(credential: JWTCredential): JWTPrincipal? {
        val id: Long? = credential.payload.getClaim("id").asLong()
        val foundUser: User? = id?.let {
            runBlocking { UserRepository.getById(it) }
        }

        return foundUser?.let {
            if (credential.payload.audience.contains(audience))
                JWTPrincipal(credential.payload)
            else null
        }
    }

    fun decodeAndVerifyToken(token: String): DecodedJWT? {
        val decodedToken = try {
            jwtVerifier.verify(token)
        } catch (ignored: Exception) {
            null
        }

        return decodedToken?.let {
            if (decodedToken.audience.contains(audience))
                decodedToken
            else null
        }
    }
}