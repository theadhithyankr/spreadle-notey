package com.mininotes.app.domain.repository

import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.SortOrder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(sortOrder: SortOrder): Flow<List<NoteEntity>>
    fun getArchivedNotes(): Flow<List<NoteEntity>>
    fun getDeletedNotes(): Flow<List<NoteEntity>>
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    suspend fun getNoteById(id: Long): NoteEntity?
    suspend fun insertNote(note: NoteEntity): Long
    suspend fun updateNote(note: NoteEntity)
    suspend fun moveNoteToTrash(note: NoteEntity)
    suspend fun restoreNoteFromTrash(note: NoteEntity)
    suspend fun permanentlyDeleteNote(note: NoteEntity)
    suspend fun emptyTrash()
}
