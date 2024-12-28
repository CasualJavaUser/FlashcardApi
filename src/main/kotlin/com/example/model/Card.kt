package com.example.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Card {
    val id: Long
    val front: String
    val back: String
    val deckId: Long
}