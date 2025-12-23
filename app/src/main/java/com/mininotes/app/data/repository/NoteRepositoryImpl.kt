package com.mininotes.app.data.repository

import com.mininotes.app.data.NoteDao
import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.SortOrder
import com.mininotes.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {

    override fun getAllNotes(sortOrder: SortOrder): Flow<List<NoteEntity>> {
        return when (sortOrder) {
            SortOrder.UPDATED_DESC -> noteDao.getNotesSortedByUpdatedDesc()
            SortOrder.UPDATED_ASC -> noteDao.getNotesSortedByUpdatedAsc()
            SortOrder.CREATED_DESC -> noteDao.getNotesSortedByCreatedDesc()
            SortOrder.CREATED_ASC -> noteDao.getNotesSortedByCreatedAsc()
            SortOrder.ALPHABETICAL_ASC -> noteDao.getNotesSortedByTitleAsc()
            SortOrder.ALPHABETICAL_DESC -> noteDao.getNotesSortedByTitleDesc()
        }
    }

    override fun getArchivedNotes(): Flow<List<NoteEntity>> = noteDao.getArchivedNotes()

    override fun getDeletedNotes(): Flow<List<NoteEntity>> = noteDao.getDeletedNotes()

    override fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    override suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    override suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    override suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    override suspend fun moveNoteToTrash(note: NoteEntity) {
        val trashedNote = note.copy(
            isDeleted = true,
            deletedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(trashedNote)
    }

    override suspend fun restoreNoteFromTrash(note: NoteEntity) {
        val restoredNote = note.copy(
            isDeleted = false,
            deletedAt = null,
            updatedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(restoredNote)
    }

    override suspend fun permanentlyDeleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    override suspend fun emptyTrash() {
        noteDao.deleteAllTrash()
    }
}
