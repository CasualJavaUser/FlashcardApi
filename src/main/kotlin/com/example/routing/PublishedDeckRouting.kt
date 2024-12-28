package com.example.routing

import com.example.model.PublishedDeck
import com.example.model.User
import com.example.repository.CardRepository
import com.example.repository.PublishedDeckRepository
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePublishedDeckRouting() {
    routing {
        authenticate("auth-jwt") {
            get("/published_decks/{id}") {
                authorize { userId ->
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    val deck = PublishedDeckRepository.getById(id)
                    if (deck != null) {
                        val users: List<User> = UserRepository.getAll()
                        val addedDecks = PublishedDeckRepository.getAddedDecks(userId)
                        deck.creatorName = users.find { it.id == deck.creatorId }?.login ?: ""
                        deck.isAdded = addedDecks.find { it.deckId == deck.id && it.userId == userId } != null
                        deck.cardCount = CardRepository.getPublishedCardCountByDeckId(deck.id)
                        deck.addCount = PublishedDeckRepository.userAddCount(deck.id)
                        call.respond(HttpStatusCode.OK, deck)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            get("/published_decks/{id}/cards") {
                authorize {
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    val deck = PublishedDeckRepository.getById(id)
                    if (deck != null) {
                        val cards = CardRepository.getPublishedCardsByDeckId(id)
                        call.respond(HttpStatusCode.OK, cards)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            get("/published_decks") {
                authorize { userId ->
                    val decks: List<PublishedDeck> = PublishedDeckRepository.getAll()
                    val users: List<User> = UserRepository.getAll()
                    val addedDecks = PublishedDeckRepository.getAddedDecks(userId)
                    decks.forEach { deck ->
                        deck.creatorName = users.find { it.id == deck.creatorId }?.login ?: ""
                        deck.isAdded = addedDecks.find { it.deckId == deck.id && it.userId == userId } != null
                        deck.cardCount = CardRepository.getPublishedCardCountByDeckId(deck.id)
                        deck.addCount = PublishedDeckRepository.userAddCount(deck.id)
                    }
                    call.respond(HttpStatusCode.OK, decks)
                }
            }

            get("/user_published_decks") {
                authorize { userId ->
                    val decks: List<PublishedDeck> = PublishedDeckRepository.getByCreatorId(userId)
                    decks.forEach { deck ->
                        deck.cardCount = CardRepository.getPublishedCardCountByDeckId(deck.id)
                        deck.addCount = PublishedDeckRepository.userAddCount(deck.id)
                    }
                    call.respond(HttpStatusCode.OK, decks)
                }
            }

            put("/add_deck/{id}") {
                authorize { userId ->
                    val deckId = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    PublishedDeckRepository.addDeck(userId, deckId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            post("/published_decks") {
                authorize { userId ->
                    val deck = call.receive<PublishedDeck>()
                    val id = PublishedDeckRepository.create(deck.name, userId)
                    call.respond(HttpStatusCode.Created, id)
                }
            }

            put("/published_decks/{id}") {
                authorize {
                    val deck = call.receive<PublishedDeck>()
                    PublishedDeckRepository.update(deck)
                    call.respond(HttpStatusCode.OK)
                }
            }

            delete("/published_decks/{id}") {
                authorize {
                    val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
                    for (publishedCard in CardRepository.getPublishedCardsByDeckId(id)) {
                        CardRepository.deletePublishedCard(publishedCard.id)
                    }
                    PublishedDeckRepository.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
