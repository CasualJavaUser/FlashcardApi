package com.example.repository

import com.example.model.Card
import com.example.model.PublishedCard
import com.example.model.StudiedCard
import com.example.repository.CardRepository.PublishedCardTable.mapPublishedCard
import com.example.repository.CardRepository.StudiedCardTable.mapStudiedCard
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date

object CardRepository {
    object CardTable : LongIdTable("card") {
        val front = varchar("front", 255)
        val back = varchar("back", 255)
    }

    object StudiedCardTable : Table("studied_card") {
        val cardId = long("card_id").uniqueIndex().references(CardTable.id, onDelete = ReferenceOption.CASCADE)
        val isNew = bool("is_new")
        val interval = integer("interval")
        val nextReview = date("next_review")
        val easiness = float("easiness")
        val reps = integer("reps")
        val deckId = long("deck_id")
            .references(StudiedDeckRepository.StudiedDeckTable.id, onDelete = ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(cardId)

        fun Query.mapStudiedCard() = map {
            StudiedCard(
                it[CardTable.id].value,
                it[CardTable.front],
                it[CardTable.back],
                it[isNew],
                it[interval],
                it[nextReview].toKotlinLocalDate(),
                it[easiness],
                it[reps],
                it[deckId]
            )
        }
    }

    object PublishedCardTable : Table("published_card") {
        val cardId = long("card_id").uniqueIndex().references(CardTable.id, onDelete = ReferenceOption.CASCADE)
        val deckId = long("deck_id")
            .references(PublishedDeckRepository.PublishedDeckTable.id, onDelete = ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(cardId)

        fun Query.mapPublishedCard() = map {
            PublishedCard(
                it[CardTable.id].value,
                it[CardTable.front],
                it[CardTable.back],
                it[deckId]
            )
        }
    }

    private suspend fun createCard(card: Card): Long = dbQuery {
        CardTable.insert {
            it[front] = card.front
            it[back] = card.back
        }[CardTable.id].value
    }

    private suspend fun updateCard(card: Card): Int = dbQuery {
        CardTable.update({ CardTable.id eq card.id }) {
            it[front] = card.front
            it[back] = card.back
        }
    }

    suspend fun create(studiedCard: StudiedCard): Long = dbQuery {
        val id: Long = createCard(studiedCard)

        StudiedCardTable.insert {
            it[cardId] = id
            it[isNew] = studiedCard.isNew
            it[interval] = studiedCard.interval
            it[nextReview] = studiedCard.nextReview.toJavaLocalDate()
            it[easiness] = studiedCard.easiness
            it[reps] = studiedCard.reps
            it[deckId] = studiedCard.deckId
        }
        id
    }

    suspend fun create(publishedCard: PublishedCard): Long = dbQuery {
        val id: Long = createCard(publishedCard)

        PublishedCardTable.insert {
            it[cardId] = id
            it[deckId] = publishedCard.deckId
        }
        id
    }

    suspend fun getStudiedCards(): List<StudiedCard> = dbQuery {
        (StudiedCardTable innerJoin CardTable).selectAll()
            .where { StudiedCardTable.cardId eq CardTable.id }
            .mapStudiedCard()
            .toList()
    }

    suspend fun getPublishedCards(): List<PublishedCard> = dbQuery {
        (PublishedCardTable innerJoin CardTable).selectAll()
            .where { PublishedCardTable.cardId eq CardTable.id }
            .mapPublishedCard()
            .toList()
    }

    suspend fun getStudiedCardById(id: Long): StudiedCard? = dbQuery {
        (StudiedCardTable innerJoin CardTable).selectAll()
            .where { (StudiedCardTable.cardId eq CardTable.id) and (CardTable.id eq id) }
            .mapStudiedCard()
            .singleOrNull()
    }

    suspend fun getPublishedCardById(id: Long): PublishedCard? = dbQuery {
        (PublishedCardTable innerJoin CardTable).selectAll()
            .where { (PublishedCardTable.cardId eq CardTable.id) and (CardTable.id eq id) }
            .mapPublishedCard()
            .singleOrNull()
    }

    suspend fun getStudiedCardsByDeckId(deckId: Long): List<StudiedCard> = dbQuery {
        (StudiedCardTable innerJoin CardTable).selectAll()
            .where { (StudiedCardTable.cardId eq CardTable.id) and (StudiedCardTable.deckId eq deckId) }
            .mapStudiedCard()
            .toList()
    }

    suspend fun getPublishedCardsByDeckId(deckId: Long): List<PublishedCard> = dbQuery {
        (PublishedCardTable innerJoin CardTable).selectAll()
            .where { (PublishedCardTable.cardId eq CardTable.id) and (PublishedCardTable.deckId eq deckId) }
            .mapPublishedCard()
            .toList()
    }

    suspend fun getStudiedCardCountByDeckId(deckId: Long): Long = dbQuery {
        StudiedCardTable.selectAll()
            .where { StudiedCardTable.deckId eq deckId }
            .count()
    }

    suspend fun getPublishedCardCountByDeckId(deckId: Long): Long = dbQuery {
        PublishedCardTable.selectAll()
            .where { PublishedCardTable.deckId eq deckId }
            .count()
    }

    suspend fun update(studiedCard: StudiedCard) = dbQuery {
        updateCard(studiedCard)
        StudiedCardTable.update({ StudiedCardTable.cardId eq studiedCard.id }) {
            it[isNew] = studiedCard.isNew
            it[interval] = studiedCard.interval
            it[nextReview] = studiedCard.nextReview.toJavaLocalDate()
            it[easiness] = studiedCard.easiness
            it[reps] = studiedCard.reps
            it[deckId] = studiedCard.deckId
        }
    }

    suspend fun update(publishedCard: PublishedCard) = dbQuery {
        updateCard(publishedCard)
        PublishedCardTable.update({ PublishedCardTable.cardId eq publishedCard.id }) {
            it[deckId] = publishedCard.deckId
        }
    }

    suspend fun deleteStudiedCard(id: Long) {
        dbQuery {
            StudiedCardTable.deleteWhere { cardId eq id }
            CardTable.deleteWhere { CardTable.id eq id }
        }
    }

    suspend fun deletePublishedCard(id: Long) {
        dbQuery {
            PublishedCardTable.deleteWhere { cardId eq id }
            CardTable.deleteWhere { CardTable.id eq id }
        }
    }
}

