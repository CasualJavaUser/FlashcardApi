package com.example.routing

import com.example.model.User
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt

fun Application.configureUserRouting() {
    routing {
        post("/users") {
            val user = call.receive<User>()
            val id = UserRepository.create(
                User(
                    user.id,
                    user.login,
                    user.email,
                    BCrypt.hashpw(user.password, BCrypt.gensalt())
                )
            )
            call.respond(HttpStatusCode.Created, id)
        }

        authenticate("auth-jwt") {
            get("/users/daily_streak") {
                authorize { userId ->
                    val dailyStreak = UserRepository.getDailyStreak(userId)
                    call.respond(HttpStatusCode.OK, "\"$dailyStreak\"")
                }
            }

            put("/users/daily_streak") {
                authorize { userId ->
                    UserRepository.updateDailyStreak(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            get("/users/review_stats") {
                authorize { userId ->
                    val reviewStats = UserRepository.getReviewStatistics(userId)
                    call.respond(HttpStatusCode.OK, "\"$reviewStats\"")
                }
            }

            put("/users/review_stats") {
                authorize { userId ->
                    UserRepository.incrementReviewCount(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            get("/users/new_card_stats") {
                authorize { userId ->
                    val newCardStats = UserRepository.getNewCardStatistics(userId)
                    call.respond(HttpStatusCode.OK, "\"$newCardStats\"")
                }
            }

            put("/users/new_card_stats") {
                authorize { userId ->
                    UserRepository.incrementNewCardCount(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
