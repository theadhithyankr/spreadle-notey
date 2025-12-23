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
    val searchQuery: String = ""
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
            searchQuery = "" // Clear search when switching views usually
        )
        getNotes()
    }

    fun createNewNote(onNoteCreated: (Long) -> Unit) {
        viewModelScope.launch {
            // We create a temp empty note to get an ID or just navigate with ID=0
            // Convention: Editor handles ID=0 as new note.
            // But if we want to insert immediately:
            /*
            val newNote = NoteEntity(title = "", content = "")
            try {
                // AddNote throws if empty, so we must allow creating empty ID 0 in UI?
                // Or NoteDao.insertNote returns ID.
                // Current AddNote use case logic:
                // if (note.id <= 0) repository.insertNote
                // But it checks for isBlank.
            } catch(e: Exception) { ... }
            */
            // Better approach: Navigate to Editor with ID -1 (new)
            // and let EditorViewModel handle the initial save or insert.
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
}
