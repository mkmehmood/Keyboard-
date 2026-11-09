package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_words")
data class UserWord(
    @PrimaryKey val word: String,
    val count: Int = 1,
    val lastUsed: Long = System.currentTimeMillis()
)

@Dao
interface UserWordDao {
    @Query("SELECT * FROM user_words ORDER BY count DESC, word ASC")
    fun observeAll(): Flow<List<UserWord>>

    @Query("SELECT * FROM user_words")
    suspend fun getAll(): List<UserWord>

    @Query("INSERT OR IGNORE INTO user_words(word, count, lastUsed) VALUES(:word, 0, :now)")
    suspend fun ensure(word: String, now: Long)

    @Query("UPDATE user_words SET count = count + 1, lastUsed = :now WHERE word = :word")
    suspend fun bump(word: String, now: Long)

    @Query("DELETE FROM user_words WHERE word = :word")
    suspend fun delete(word: String)

    @Query("DELETE FROM user_words")
    suspend fun clear()
}
