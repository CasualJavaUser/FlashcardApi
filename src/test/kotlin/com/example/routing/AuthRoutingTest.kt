package com.example.routing

import com.example.configureDatabase
import com.example.login
import com.example.model.AuthResponse
import com.example.model.User
import com.example.service.JwtService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutingTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun loginValid() = testApplication {
        application {
            configureDatabase()
            configureAuthRouting(JwtService(this))
        }
        val response = login(User(1, "", "f"))
        assertEquals(response.status, HttpStatusCode.OK)
        val body = json.decodeFromString<AuthResponse>(response.body())
        assertTrue { body.accessToken.isNotEmpty() && body.refreshToken.isNotEmpty() }
    }

    @Test
    fun loginInvalid() = testApplication {
        application {
            configureDatabase()
            configureAuthRouting(JwtService(this))
        }
        val response = login(User(1, "a", "a"))
        assertEquals(response.status, HttpStatusCode.NotFound)
    }

    @Test
    fun refresh() = testApplication {
        application {
            configureDatabase()
            configureAuthRouting(JwtService(this))
        }
        val loginResponse = login(User(1, "", "f"))
        val token = json.decodeFromString<AuthResponse>(loginResponse.body()).refreshToken
        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody("\"$token\"")
        }
        assertEquals(response.status, HttpStatusCode.OK)
        assertTrue { (response.body() as String).isNotEmpty() }
    }
}