package com.example.routing

import com.example.model.StudiedCard
import com.example.repository.CardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureStudiedCardRouting() {
    routing {
        authenticate("auth-jwt") {
            get("/studied_cards/{id}") {
                authorize {
                    val id = getParameterOrThrow("id").toLong()
                    val card = CardRepository.getStudiedCardById(id)
                    if (card != null) {
                        call.respond(HttpStatusCode.OK, card)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            get("/studied_cards") {
                authorize {
                    val cards: List<StudiedCard> = CardRepository.getStudiedCards()
                    call.respond(HttpStatusCode.OK, cards)
                }
            }

            post("/studied_cards") {
                authorize {
                    val card = call.receive<StudiedCard>()
                    val id = CardRepository.create(card)
                    call.respond(HttpStatusCode.Created, id)
                }
            }

            put("/studied_cards") {
                authorize {
                    val card = call.receive<StudiedCard>()
                    CardRepository.update(card)
                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/studied_cards/{id}") {
                authorize {
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    CardRepository.deleteStudiedCard(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
