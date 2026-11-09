package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClipboardRepository(
    private val context: Context,
    private val clipboardDao: ClipboardDao
) {
    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val allClips: Flow<List<ClipboardItem>> = clipboardDao.getAllClips()
    val pinnedClips: Flow<List<ClipboardItem>> = clipboardDao.getPinnedClips()
    val recentClips: Flow<List<ClipboardItem>> = clipboardDao.getRecentClips()
    val totalCount: Flow<Int> = clipboardDao.getCount()
    val pinnedCount: Flow<Int> = clipboardDao.getPinnedCount()

    fun searchClips(query: String): Flow<List<ClipboardItem>> {
        return if (query.isBlank()) {
            allClips
        } else {
            clipboardDao.searchClips(query.trim())
        }
    }

    fun getClipsByCategory(category: String): Flow<List<ClipboardItem>> {
        return if (category == "All") {
            allClips
        } else {
            clipboardDao.getClipsByCategory(category)
        }
    }

    suspend fun insertClip(text: String, isPinned: Boolean = false, category: String = "General"): Long {
        if (text.isBlank()) return -1
        val trimmed = text.trim()
        val existing = clipboardDao.findByText(trimmed)
        return if (existing != null) {
            // Update timestamp to bump to top
            clipboardDao.update(existing.copy(timestamp = System.currentTimeMillis()))
            existing.id
        } else {
            clipboardDao.insert(
                ClipboardItem(
                    text = trimmed,
                    timestamp = System.currentTimeMillis(),
                    isPinned = isPinned,
                    category = category,
                    characterCount = trimmed.length
                )
            )
        }
    }

    suspend fun togglePin(item: ClipboardItem) {
        clipboardDao.update(item.copy(isPinned = !item.isPinned))
    }

    suspend fun updateClip(item: ClipboardItem) {
        clipboardDao.update(item)
    }

    suspend fun deleteClip(item: ClipboardItem) {
        clipboardDao.delete(item)
    }

    suspend fun clearRecentClips() {
        clipboardDao.clearRecentClips()
    }

    suspend fun clearAll() {
        clipboardDao.clearAll()
    }

    suspend fun getLatestClip(): ClipboardItem? {
        return clipboardDao.getLatestClip()
    }

    /**
     * Captures current content from Android system clipboard and saves to unlimited DB
     */
    fun syncSystemClipboard(onNewClip: ((ClipboardItem) -> Unit)? = null) {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0)?.coerceToText(context)?.toString()
            if (!text.isNullOrBlank()) {
                repositoryScope.launch {
                    val existing = clipboardDao.findByText(text.trim())
                    if (existing == null) {
                        val newItem = ClipboardItem(
                            text = text.trim(),
                            timestamp = System.currentTimeMillis(),
                            isPinned = false,
                            category = detectCategory(text),
                            characterCount = text.length
                        )
                        val id = clipboardDao.insert(newItem)
                        onNewClip?.invoke(newItem.copy(id = id))
                    }
                }
            }
        }
    }

    /**
     * Copies text to system clipboard and stores in database
     */
    fun copyToSystemClipboard(text: String, category: String = "General") {
        if (text.isBlank()) return
        val clip = ClipData.newPlainText("Gboard Clip", text)
        clipboardManager?.setPrimaryClip(clip)
        repositoryScope.launch {
            insertClip(text, category = category)
        }
    }

    private fun detectCategory(text: String): String {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> "Link"
            trimmed.contains("@") && trimmed.contains(".") && !trimmed.contains(" ") -> "Email"
            trimmed.lines().size > 3 || trimmed.contains("val ") || trimmed.contains("function") || trimmed.contains("{") -> "Code"
            trimmed.matches(Regex("^[0-9+\\-() ]{7,}$")) -> "Phone"
            else -> "General"
        }
    }
}
