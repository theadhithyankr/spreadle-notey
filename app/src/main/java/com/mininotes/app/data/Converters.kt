package com.mininotes.app.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun fromNoteType(value: NoteType): String = value.name

    @TypeConverter
    fun toNoteType(value: String): NoteType = try {
        NoteType.valueOf(value)
    } catch (e: Exception) {
        NoteType.TEXT
    }

    @TypeConverter
    fun fromChecklist(value: List<ChecklistItem>): String = json.encodeToString(value)

    @TypeConverter
    fun toChecklist(value: String): List<ChecklistItem> = try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}
