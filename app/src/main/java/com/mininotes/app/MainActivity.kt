package com.mininotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mininotes.app.data.NotesDatabase
import com.mininotes.app.data.repository.NoteRepositoryImpl
import com.mininotes.app.domain.repository.NoteRepository
import com.mininotes.app.domain.usecase.*
import com.mininotes.app.navigation.NavGraph
import com.mininotes.app.ui.editor.EditorViewModel
import com.mininotes.app.ui.notes.NotesViewModel
import com.mininotes.app.ui.theme.MiniNotesTheme
import com.mininotes.app.ui.trash.TrashViewModel
import androidx.room.Room
import com.mininotes.app.data.repository.ThemeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

class MainActivity : ComponentActivity() {

    private lateinit var noteUseCases: NoteUseCases
    private lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NotesDatabase.getDatabase(applicationContext)
        val repository = NoteRepositoryImpl(database.noteDao())
        
        noteUseCases = NoteUseCases(
            getNotes = GetNotes(repository),
            getArchivedNotes = GetArchivedNotes(repository),
            searchNotes = SearchNotes(repository),
            deleteNote = DeleteNote(repository),
            addNote = AddNote(repository),
            getNote = GetNote(repository),
            restoreNote = RestoreNote(repository),
            permanentlyDeleteNote = PermanentlyDeleteNote(repository),
            getDeletedNotes = GetDeletedNotes(repository),
            emptyTrash = EmptyTrash(repository)
        )

        themeRepository = ThemeRepository(applicationContext)
        
        // Load initial theme synchronously to prevent flash
        val initialTheme = runBlocking { themeRepository.theme.first() }

        setContent {
            val currentTheme by themeRepository.theme.collectAsState(initial = initialTheme)
            
            MiniNotesTheme(appTheme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    NavGraph(
                        navController = navController,
                        noteUseCases = noteUseCases,
                        themeRepository = themeRepository
                    )
                }
            }
        }
    }
}

class ViewModelFactory(private val useCases: NoteUseCases) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(useCases) as T
            }
            modelClass.isAssignableFrom(EditorViewModel::class.java) -> {
                EditorViewModel(useCases) as T
            }
            modelClass.isAssignableFrom(TrashViewModel::class.java) -> {
                TrashViewModel(useCases) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
