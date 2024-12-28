package com.example.repository

import com.example.model.StudiedDeck
import com.example.repository.StudiedDeckRepository.StudiedDeckTable.mapStudiedDeck
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date

object StudiedDeckRepository {
    object StudiedDeckTable : LongIdTable("studied_deck") {
        val name = varchar("name", 255)
        val lastStudied = date("last_studied")
        val userId = long("user_id").references(UserRepository.UserTable.id)

        fun Query.mapStudiedDeck() = map {
            StudiedDeck(
                it[id].value,
                it[name],
                it[lastStudied].toKotlinLocalDate(),
                it[userId]
            )
        }
    }

    suspend fun create(studiedDeck: StudiedDeck): Long = dbQuery {
        StudiedDeckTable.insert {
            it[name] = studiedDeck.name
            it[lastStudied] = studiedDeck.lastStudied.toJavaLocalDate()
            it[userId] = studiedDeck.userId
        }[StudiedDeckTable.id].value
    }

    suspend fun getByUserId(userId: Long): List<StudiedDeck> = dbQuery {
        StudiedDeckTable.selectAll()
            .where { StudiedDeckTable.userId eq userId }
            .mapStudiedDeck()
            .toList()
    }

    suspend fun getById(id: Long): StudiedDeck? = dbQuery {
        StudiedDeckTable.selectAll()
            .where { StudiedDeckTable.id eq id }
            .mapStudiedDeck()
            .singleOrNull()
    }

    suspend fun update(id: Long, newName: String? = null, newDate: LocalDate? = null) = dbQuery {
        StudiedDeckTable.update({ StudiedDeckTable.id eq id }) { updateStmt ->
            newName?.let { updateStmt[name] = it }
            newDate?.let { updateStmt[lastStudied] = it.toJavaLocalDate() }
        }
    }

    suspend fun delete(id: Long) = dbQuery {
        StudiedDeckTable.deleteWhere { StudiedDeckTable.id.eq(id) }
    }
}

