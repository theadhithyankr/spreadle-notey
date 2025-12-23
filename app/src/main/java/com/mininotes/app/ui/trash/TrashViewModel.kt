package com.mininotes.app.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mininotes.app.data.NoteEntity
import com.mininotes.app.domain.usecase.NoteUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val noteUseCases: NoteUseCases) : ViewModel() {
    
    val deletedNotes: StateFlow<List<NoteEntity>> = noteUseCases.getDeletedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreNote(note: NoteEntity) {
        viewModelScope.launch {
            noteUseCases.restoreNote(note)
        }
    }

    fun permanentlyDeleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteUseCases.permanentlyDeleteNote(note)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteUseCases.emptyTrash()
        }
    }
}
