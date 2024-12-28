package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class AddedDeck (
    val userId: Long,
    val deckId: Long
)