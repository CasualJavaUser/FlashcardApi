package com.example

import com.example.model.User
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

suspend fun ApplicationTestBuilder.login(user: User): HttpResponse = client.post("/login") {
    contentType(ContentType.Application.Json)
    setBody(json.encodeToString(user))
}