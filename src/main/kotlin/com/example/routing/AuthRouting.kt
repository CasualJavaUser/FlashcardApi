package com.example.routing

import com.example.model.AuthResponse
import com.example.model.User
import com.example.repository.RefreshTokenRepository
import com.example.repository.UserRepository
import com.example.service.JwtService
import org.mindrot.jbcrypt.BCrypt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureAuthRouting(jwtService: JwtService) {
    routing {
        post("/login") {
            val user = call.receive<User>()

            val foundUser = UserRepository.getByLogin(user.login)

            foundUser?.let {
                if (!BCrypt.checkpw(user.password, foundUser.password)) {
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    val accessToken = jwtService.createAccessToken(it.id)
                    val refreshToken = jwtService.createRefreshToken(it.id)
                    RefreshTokenRepository.saveToken(refreshToken, it.id)

                    call.respond(HttpStatusCode.OK, AuthResponse(accessToken, refreshToken))
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        post("/refresh") {
            val refreshToken = call.receive<String>().trim('\"')
            val decodedRefreshToken = jwtService.decodeAndVerifyToken(refreshToken)
            val savedId = RefreshTokenRepository.findIdByToken(refreshToken)

            if (decodedRefreshToken != null && savedId != null) {
                val idFromToken: Long? = decodedRefreshToken.getClaim("id").asLong()

                if (UserRepository.exists(savedId) && idFromToken == savedId) {
                    val newToken = jwtService.createAccessToken(savedId)
                    call.respond(HttpStatusCode.OK, '"' + newToken + '"')
                }
                else call.respond(HttpStatusCode.Unauthorized, "user not found or login from token not matching found user")
            }
            else call.respond(HttpStatusCode.Unauthorized, "decoded token not verified or saved login is null")
        }
    }
}