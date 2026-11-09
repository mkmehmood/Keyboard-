package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_items",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["isPinned"]),
        Index(value = ["category"])
    ]
)
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val category: String = "General",
    val characterCount: Int = text.length,
    val sourcePackage: String = "System Clipboard"
)
