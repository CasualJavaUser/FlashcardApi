package com.example.plugins

import com.example.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(jwtService: JwtService) {

    authentication {
        jwt("auth-jwt") {
            realm = jwtService.realm
            verifier(jwtService.jwtVerifier)

            validate { credential ->
                jwtService.validate(credential)
            }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
