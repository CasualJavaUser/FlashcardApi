package com.example.repository

import com.example.model.User
import com.example.repository.UserRepository.UserTable.dailyStreak
import com.example.repository.UserRepository.UserTable.id
import com.example.repository.UserRepository.UserTable.lastNewCardCountUpdate
import com.example.repository.UserRepository.UserTable.lastReviewCountUpdate
import com.example.repository.UserRepository.UserTable.mapUser
import com.example.repository.UserRepository.UserTable.newCardStats
import com.example.repository.UserRepository.UserTable.reviewStats
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object UserRepository {
    object UserTable : LongIdTable("user") {
        val login = varchar("login", length = 255).uniqueIndex()
        val email = varchar("email", length = 255).uniqueIndex()
        val password = varchar("password", length = 255)
        val dailyStreak = integer("daily_streak")
        val reviewStats = varchar("review_stats", 255)
        val newCardStats = varchar("new_card_stats", 255)
        val lastStreakUpdate = date("last_streak_update").nullable()
        val lastReviewCountUpdate = date("last_review_count_update").nullable()
        val lastNewCardCountUpdate = date("last_new_card_count_update").nullable()

        fun Query.mapUser() = map {
            User(
                it[id].value,
                it[login],
                it[email],
                it[password]
            )
        }
    }

    suspend fun create(user: User): Long = dbQuery {
        UserTable.insert {
            it[login] = user.login
            it[email] = user.email
            it[password] = user.password
        }[UserTable.id].value
    }

    suspend fun getAll(): List<User> = dbQuery {
        UserTable.selectAll().mapUser().toList()
    }

    suspend fun getById(id: Long): User? = dbQuery {
        UserTable.selectAll().where { UserTable.id eq id }.mapUser().singleOrNull()
    }

    suspend fun exists(id: Long): Boolean {
        return dbQuery {
            !UserTable.selectAll().where { UserTable.id eq id }.empty()
        }
    }

    suspend fun getByLogin(login: String): User? = dbQuery {
        UserTable.selectAll().where { UserTable.login eq login }.mapUser().singleOrNull()
    }

    suspend fun update(user: User) = dbQuery {
        UserTable.update({ UserTable.id eq user.id }) {
            it[login] = user.login
            it[email] = user.email
            it[password] = user.password
        }
    }

    suspend fun delete(id: Long) = dbQuery {
        UserTable.deleteWhere { UserTable.id.eq(id) }
    }

    suspend fun getDailyStreak(id: Long): Int = dbQuery {
        UserTable.selectAll().where { UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            val lastUpdate = userResultRow[UserTable.lastStreakUpdate]
            val isStreakActive = !(lastUpdate == null || lastUpdate.until(LocalDate.now(), ChronoUnit.DAYS) > 1)

            if (!isStreakActive)
                return@let 0
            userResultRow[dailyStreak]
        }
    }

    suspend fun updateDailyStreak(id: Long) = dbQuery {
        UserTable.selectAll().where { UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            val lastUpdate = userResultRow[UserTable.lastStreakUpdate]
            val isStreakActive = !(lastUpdate == null || lastUpdate.until(LocalDate.now(), ChronoUnit.DAYS) > 1)

            if(!isStreakActive) {
                UserTable.update({ UserTable.id eq id }) { it[dailyStreak] = 1 }
            } else if (!lastUpdate!!.isBefore(LocalDate.now())) {
                val updatedStreak = userResultRow[dailyStreak]+1
                UserTable.update({ UserTable.id eq id }) { it[dailyStreak] = updatedStreak }
            }
            UserTable.update { it[lastStreakUpdate] = LocalDate.now() }
        }
    }

    suspend fun getReviewStatistics(id: Long): String = dbQuery {
        UserTable.selectAll().where{ UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            if (userResultRow[lastReviewCountUpdate]?.isBefore(LocalDate.now()) != false)
                updateCount(reviewStats, lastReviewCountUpdate, userResultRow)

            userResultRow[reviewStats]
        }
    }

    suspend fun incrementReviewCount(id: Long) = dbQuery {
        UserTable.selectAll().where{ UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            if (userResultRow[lastReviewCountUpdate]?.isBefore(LocalDate.now()) != false)
                updateCount(reviewStats, lastReviewCountUpdate, userResultRow)

            val updatedStats = userResultRow[reviewStats].deserializeArray().also { it[it.lastIndex]++ }
            UserTable.update { it[reviewStats] = updatedStats.serialize() }
        }
    }

    suspend fun getNewCardStatistics(id: Long): String = dbQuery {
        UserTable.selectAll().where{ UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            if (userResultRow[lastNewCardCountUpdate]?.isBefore(LocalDate.now()) != false)
                updateCount(newCardStats, lastNewCardCountUpdate, userResultRow)

            userResultRow[newCardStats]
        }
    }

    suspend fun incrementNewCardCount(id: Long) = dbQuery {
        UserTable.selectAll().where{ UserTable.id eq id }.singleOrNull()!!.let { userResultRow ->
            if (userResultRow[lastNewCardCountUpdate]?.isBefore(LocalDate.now()) != false)
                updateCount(newCardStats, lastNewCardCountUpdate, userResultRow)

            val updatedStats = userResultRow[newCardStats].deserializeArray().also { it[it.lastIndex]++ }
            UserTable.update { it[newCardStats] = updatedStats.serialize() }
        }
    }

    private fun updateCount(
        countColumn: Column<String>,
        lastUpdateColumn: Column<LocalDate?>,
        userResultRow: ResultRow
    ) {
        userResultRow[lastUpdateColumn]?.let { lastUpdate ->
            lastUpdate.until(LocalDate.now(), ChronoUnit.DAYS).toInt().let { offset ->
                if (offset != 0) {
                    val updatedStats = userResultRow[countColumn].deserializeArray()
                    for (i in updatedStats.indices) {
                        if (i - offset >= 0)
                            updatedStats[i-offset] = updatedStats[i]
                        updatedStats[i] = 0
                    }
                    UserTable.update({ UserTable.id eq userResultRow[UserTable.id] }) {
                        it[countColumn] = updatedStats.serialize()
                    }
                }
            }
        }
        UserTable.update({ UserTable.id eq userResultRow[id] }) {
            it[lastUpdateColumn] = LocalDate.now()
        }
    }

    private fun IntArray.serialize(): String = Json.encodeToString(IntArraySerializer(), this)

    private fun String.deserializeArray(): IntArray = try {
        Json.decodeFromString<IntArray>(this)
    } catch (e: Exception) {
        IntArray(10)
    }
}

