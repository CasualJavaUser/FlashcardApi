package com.example.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class StudiedDeck(
    val id: Long,
    val name: String,
    val lastStudied: LocalDate,
    val userId: Long,
    var cardCount: Long = 0L
)
