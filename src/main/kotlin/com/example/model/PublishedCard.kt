package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class PublishedCard(
    override val id: Long,
    override val front: String,
    override val back: String,
    override val deckId: Long
): Card