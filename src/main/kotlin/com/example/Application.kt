package com.example

import com.example.repository.CardRepository
import com.example.repository.PublishedDeckRepository
import com.example.repository.StudiedDeckRepository
import com.example.repository.UserRepository
import com.example.routing.*
import com.example.service.JwtService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

lateinit var database: Database

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
            }
        )
    }

    val jwtService = JwtService(this)

    configureSecurity(jwtService)
    configureAuthRouting(jwtService)
    configureUserRouting()
    configureStudiedCardRouting()
    configureStudiedDeckRouting()
    configurePublishedCardRouting()
    configurePublishedDeckRouting()

    val tables = listOf(
        CardRepository.CardTable,
        CardRepository.StudiedCardTable,
        CardRepository.PublishedCardTable,
        StudiedDeckRepository.StudiedDeckTable,
        PublishedDeckRepository.PublishedDeckTable,
        PublishedDeckRepository.AddedDeckTable,
        UserRepository.UserTable
    )

    for (table in tables) {
        transaction(database) {
            SchemaUtils.create(table)
        }
    }
}

fun configureDatabase() {
    database = Database.connect(
        url = "jdbc:mariadb://localhost:3306/flashcards",
        user = "root",
        driver = "org.mariadb.jdbc.Driver",
        password = "",
    )
}

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
