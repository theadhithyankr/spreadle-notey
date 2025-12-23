package com.mininotes.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.SortOrder
import com.mininotes.app.domain.usecase.NoteUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val showArchived: Boolean = false,
    val searchQuery: String = "",
    val selectedNoteIds: Set<Long> = emptySet()
)

class NotesViewModel(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState

    private var getNotesJob: Job? = null

    init {
        getNotes()
    }

    private fun getNotes() {
        getNotesJob?.cancel()
        
        val state = _uiState.value
        val flow = when {
            state.searchQuery.isNotEmpty() -> noteUseCases.searchNotes(state.searchQuery)
            state.showArchived -> noteUseCases.getArchivedNotes()
            else -> noteUseCases.getNotes(state.sortOrder)
        }
        
        getNotesJob = flow.onEach { notes ->
            _uiState.value = _uiState.value.copy(notes = notes)
        }.launchIn(viewModelScope)
    }

    // ... (Existing methods: updateSearchQuery, updateSortOrder, toggleArchiveView, createNewNote) ... 
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        getNotes()
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        getNotes()
    }

    fun toggleArchiveView() {
        val newState = !_uiState.value.showArchived
        _uiState.value = _uiState.value.copy(
            showArchived = newState,
            searchQuery = "",
            selectedNoteIds = emptySet() // Clear selection on view switch
        )
        getNotes()
    }

    fun createNewNote(onNoteCreated: (Long) -> Unit) {
        viewModelScope.launch {
            onNoteCreated(-1L) 
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch {
            noteUseCases.addNote(note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun toggleArchiveNote(note: NoteEntity) {
        viewModelScope.launch {
            noteUseCases.addNote(note.copy(
                isArchived = !note.isArchived,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun moveToTrash(note: NoteEntity) {
        viewModelScope.launch {
            noteUseCases.deleteNote(note)
        }
    }
    
    // Selection Logic
    fun startSelection(noteId: Long) {
        _uiState.value = _uiState.value.copy(selectedNoteIds = setOf(noteId))
    }
    
    fun toggleSelection(noteId: Long) {
        val current = _uiState.value.selectedNoteIds.toMutableSet()
        if (current.contains(noteId)) current.remove(noteId) else current.add(noteId)
        
        _uiState.value = _uiState.value.copy(selectedNoteIds = current)
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedNoteIds = emptySet())
    }
    
    fun deleteSelectedNotes() {
        val selectedIds = _uiState.value.selectedNoteIds
        viewModelScope.launch {
            val notesToDelete = _uiState.value.notes.filter { it.id in selectedIds }
            notesToDelete.forEach { note ->
                noteUseCases.deleteNote(note)
            }
            clearSelection()
        }
    }
    
    fun pinSelectedNotes(pin: Boolean) {
        val selectedIds = _uiState.value.selectedNoteIds
        viewModelScope.launch {
            val notesToUpdate = _uiState.value.notes.filter { it.id in selectedIds }
            notesToUpdate.forEach { note ->
                noteUseCases.addNote(note.copy(isPinned = pin, updatedAt = System.currentTimeMillis()))
            }
            clearSelection()
        }
    }
}
