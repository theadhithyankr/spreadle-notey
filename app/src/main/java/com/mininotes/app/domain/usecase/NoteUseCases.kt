package com.mininotes.app.domain.usecase

import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.SortOrder
import com.mininotes.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotes(private val repository: NoteRepository) {
    operator fun invoke(
        sortOrder: SortOrder = SortOrder.UPDATED_DESC
    ): Flow<List<NoteEntity>> {
        return repository.getAllNotes(sortOrder)
    }
}

class GetNote(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long): NoteEntity? {
        // Return null if id is invalid (<= 0)
        if (id <= 0) return null
        return repository.getNoteById(id)
    }
}

class AddNote(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity): Long {
        if (note.title.isBlank() && note.content.isBlank() && note.checklistItems.isEmpty()) {
            throw InvalidNoteException("The title and content of the note can't be empty.")
        }
        return if (note.id <= 0) {
            repository.insertNote(note)
        } else {
            repository.updateNote(note)
            note.id
        }
    }
}

class DeleteNote(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) {
        repository.moveNoteToTrash(note)
    }
}

class RestoreNote(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) {
        repository.restoreNoteFromTrash(note)
    }
}

class PermanentlyDeleteNote(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) {
        repository.permanentlyDeleteNote(note)
    }
}

class InvalidNoteException(message: String) : Exception(message)
