package com.example.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable()
data class StudiedCard(
    override val id: Long,
    override var front: String,
    override var back: String,
    var isNew: Boolean,
    var interval: Int,
    var nextReview: LocalDate,
    var easiness: Float,
    var reps: Int,
    override val deckId: Long
): Card