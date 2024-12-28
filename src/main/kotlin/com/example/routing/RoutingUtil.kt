package com.example.routing

import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<Unit, ApplicationCall>.authorize(body: suspend (userId: Long) -> Unit) {
    val principal = call.principal<JWTPrincipal>()
    val userId: Long? = principal?.payload?.getClaim("id")?.asLong()
    if ( userId != null && UserRepository.exists(userId)) {
        body.invoke(userId)
    } else {
        call.respond(HttpStatusCode.Unauthorized)
    }
}

fun PipelineContext<Unit, ApplicationCall>.getParameterOrThrow(param: String): String =
    call.parameters[param] ?: throw IllegalArgumentException("Invalid $param")
