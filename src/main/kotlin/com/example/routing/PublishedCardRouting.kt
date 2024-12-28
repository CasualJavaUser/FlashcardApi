package com.example.routing

import com.example.model.PublishedCard
import com.example.repository.CardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePublishedCardRouting() {
    routing {
        authenticate("auth-jwt") {
            get("/published_cards/{id}") {
                authorize {
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    val card = CardRepository.getPublishedCardById(id)
                    if (card != null) {
                        call.respond(HttpStatusCode.OK, card)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            get("/published_cards") {
                authorize {
                    val cards: List<PublishedCard> = CardRepository.getPublishedCards()
                    call.respond(HttpStatusCode.OK, cards)
                }
            }

            post("/published_cards") {
                authorize {
                    val card = call.receive<PublishedCard>()
                    val id = CardRepository.create(card)
                    call.respond(HttpStatusCode.Created, id)
                }
            }

            put("/published_cards/{id}") {
                authorize {
                    val card = call.receive<PublishedCard>()
                    CardRepository.update(card)
                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/published_cards/{id}") {
                authorize {
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    CardRepository.deletePublishedCard(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
