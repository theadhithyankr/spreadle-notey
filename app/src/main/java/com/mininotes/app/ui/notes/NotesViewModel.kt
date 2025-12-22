package com.mininotes.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mininotes.app.data.NoteDao
import com.mininotes.app.data.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val noteDao: NoteDao) : ViewModel() {
    
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createNewNote(onNoteCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = NoteEntity(
                title = "",
                content = "",
                updatedAt = System.currentTimeMillis()
            )
            val noteId = noteDao.insertNote(newNote)
            onNoteCreated(noteId)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
}
