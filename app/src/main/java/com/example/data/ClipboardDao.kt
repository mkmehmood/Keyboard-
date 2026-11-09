package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClips(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedClips(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 0 ORDER BY timestamp DESC")
    fun getRecentClips(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE text LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchClips(query: String): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getClipsByCategory(category: String): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestClip(): ClipboardItem?

    @Query("SELECT * FROM clipboard_items WHERE text = :text LIMIT 1")
    suspend fun findByText(text: String): ClipboardItem?

    @Query("SELECT COUNT(*) FROM clipboard_items")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 1")
    fun getPinnedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Update
    suspend fun update(item: ClipboardItem)

    @Delete
    suspend fun delete(item: ClipboardItem)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearRecentClips()

    @Query("DELETE FROM clipboard_items")
    suspend fun clearAll()
}
