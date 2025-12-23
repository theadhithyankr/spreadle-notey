package com.mininotes.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class NoteType {
    TEXT,
    CHECKLIST
}

@Serializable
data class ChecklistItem(
    val text: String,
    val isChecked: Boolean
)

@Serializable
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String, // Holds plain text or legacy content
    val type: NoteType = NoteType.TEXT,
    val checklistItems: List<ChecklistItem> = emptyList(), // Serialized automatically by TypeConverter if we set one up, OR we handle serialization manually. To keep it simple and efficient with Room + Serialization, we might store this as JSON string in DB, but defining it here as List is cleaner for domain if we use a TypeConverter.
    val richSpansJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val color: String? = null,
    val labels: String = "" // Comma separated tags
)
