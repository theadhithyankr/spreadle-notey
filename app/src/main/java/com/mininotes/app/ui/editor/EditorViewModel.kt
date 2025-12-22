package com.mininotes.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mininotes.app.data.NoteDao
import com.mininotes.app.data.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = true
)

class EditorViewModel(private val noteDao: NoteDao) : ViewModel() {
    
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            val note = noteDao.getNoteById(noteId)
            if (note != null) {
                _state.value = EditorState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _state.value = _state.value.copy(title = newTitle)
        saveNote()
    }

    fun updateContent(newContent: String) {
        _state.value = _state.value.copy(content = newContent)
        saveNote()
    }

    private fun saveNote() {
        val currentState = _state.value
        if (currentState.noteId > 0) {
            viewModelScope.launch {
                val updatedNote = NoteEntity(
                    id = currentState.noteId,
                    title = currentState.title,
                    content = currentState.content,
                    updatedAt = System.currentTimeMillis()
                )
                noteDao.updateNote(updatedNote)
            }
        }
    }
}
