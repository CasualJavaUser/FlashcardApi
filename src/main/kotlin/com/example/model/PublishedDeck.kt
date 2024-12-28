package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class PublishedDeck(
    val id: Long,
    val name: String,
    val creatorId: Long,
    var creatorName: String = "",
    var isAdded: Boolean = false,
    var addCount: Long = 0L,
    var cardCount: Long = 0L
)
