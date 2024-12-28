package com.example.repository

import com.example.model.AddedDeck
import com.example.model.PublishedDeck
import com.example.repository.PublishedDeckRepository.AddedDeckTable.mapAddedDeck
import com.example.repository.PublishedDeckRepository.PublishedDeckTable.mapPublishedDeck
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object PublishedDeckRepository {
    object PublishedDeckTable : LongIdTable("published_deck") {
        val name = varchar("name", 255)
        val creatorId = long("creator_id")
            .references(UserRepository.UserTable.id)

        fun Query.mapPublishedDeck() = map {
            PublishedDeck(
                it[id].value,
                it[name],
                it[creatorId]
            )
        }

        init {
            uniqueIndex(name, creatorId)
        }
    }

    object AddedDeckTable : Table("added_deck") {
        val userId = long("user_id")
        val deckId = long("deck_id")

        override val primaryKey = PrimaryKey(userId, deckId)

        fun Query.mapAddedDeck() = map {
            AddedDeck(
                it[userId],
                it[deckId],
            )
        }
    }

    suspend fun create(name: String, creatorId: Long): Long = dbQuery {
        PublishedDeckTable.insert {
            it[PublishedDeckTable.name] = name
            it[PublishedDeckTable.creatorId] = creatorId
        }[PublishedDeckTable.id].value
    }

    suspend fun getAll(): List<PublishedDeck> = dbQuery {
        PublishedDeckTable.selectAll()
            .mapPublishedDeck()
            .toList()
    }

    suspend fun getByCreatorId(creatorId: Long): List<PublishedDeck> = dbQuery {
        PublishedDeckTable.selectAll()
            .where { PublishedDeckTable.creatorId eq creatorId }
            .mapPublishedDeck()
            .toList()
    }

    suspend fun getById(id: Long): PublishedDeck? = dbQuery {
        PublishedDeckTable.selectAll()
            .where { PublishedDeckTable.id eq id }
            .mapPublishedDeck()
            .singleOrNull()
    }

    suspend fun addDeck(userId: Long, deckId: Long) = dbQuery {
        AddedDeckTable.insertIgnore {
            it[AddedDeckTable.userId] = userId
            it[AddedDeckTable.deckId] = deckId
        }
    }

    suspend fun getAddedDecks(userId: Long): List<AddedDeck> = dbQuery {
        AddedDeckTable.selectAll().where { AddedDeckTable.userId eq userId }.mapAddedDeck()
    }

    suspend fun userAddCount(deckId: Long): Long = dbQuery {
        AddedDeckTable.selectAll().where { AddedDeckTable.deckId eq deckId }.count()
    }

    suspend fun update(publishedDeck: PublishedDeck) = dbQuery {
        PublishedDeckTable.update({ PublishedDeckTable.id eq publishedDeck.id }) {
            it[name] = publishedDeck.name
        }
    }

    suspend fun delete(id: Long) = dbQuery {
        PublishedDeckTable.deleteWhere { PublishedDeckTable.id.eq(id) }
    }
}

