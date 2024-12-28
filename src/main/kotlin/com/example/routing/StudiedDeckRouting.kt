package com.example.routing

import com.example.model.StudiedDeck
import com.example.repository.CardRepository
import com.example.repository.StudiedDeckRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Application.configureStudiedDeckRouting() {
    routing {
        authenticate("auth-jwt") {
            get("/studied_decks") {
                authorize { userId ->
                    val decks: List<StudiedDeck> = StudiedDeckRepository.getByUserId(userId)
                    decks.forEach { deck ->
                        deck.cardCount = CardRepository.getStudiedCardCountByDeckId(deck.id)
                    }
                    call.respond(HttpStatusCode.OK, decks)
                }
            }

            get("/studied_decks/{id}") {
                authorize { userId ->
                    val id = getParameterOrThrow("id").toLong()
                    val deck = StudiedDeckRepository.getById(id)
                    if (deck != null && deck.userId == userId) {
                        deck.cardCount = CardRepository.getStudiedCardCountByDeckId(deck.id)
                        call.respond(HttpStatusCode.OK, deck)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            get("studied_decks/{id}/cards") {
                authorize { userId ->
                    val deckId = getParameterOrThrow("id").toLong()
                    val deck = StudiedDeckRepository.getById(deckId)
                    if (deck != null && deck.userId == userId) {
                        val cards = CardRepository.getStudiedCardsByDeckId(deckId)
                        call.respond(HttpStatusCode.OK, cards)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            post("/studied_decks") {
                authorize { userId ->
                    val deck = call.receive<StudiedDeck>()
                    val id = StudiedDeckRepository.create(deck.copy(userId = userId))
                    call.respond(HttpStatusCode.Created, id)
                }
            }

            put("/studied_decks/rename/{id}") {
                authorize {
                    val deckId = getParameterOrThrow("id").toLong()
                    val name = call.receive<String>()
                    StudiedDeckRepository.update(deckId, newName = name.substring(1, name.length-1))
                    call.respond(HttpStatusCode.OK)
                }
            }

            put("/studied_decks/{id}") {
                authorize {
                    val deckId = getParameterOrThrow("id").toLong()
                    val date = call.receive<LocalDate>()
                    StudiedDeckRepository.update(deckId, newDate = date)
                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/studied_decks/{id}") {
                authorize {
                    val id = getParameterOrThrow("id").toLong()
                    for (studiedCard in CardRepository.getStudiedCardsByDeckId(id)) {
                        CardRepository.deleteStudiedCard(studiedCard.id)
                    }
                    StudiedDeckRepository.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
