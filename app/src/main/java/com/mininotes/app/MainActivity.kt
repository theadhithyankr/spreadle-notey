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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NotesDatabase.getDatabase(applicationContext)
        val repository = NoteRepositoryImpl(database.noteDao())
        
        val useCases = NoteUseCases(
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

        setContent {
            MiniNotesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    val notesViewModel: NotesViewModel = viewModel(
                        factory = ViewModelFactory(useCases)
                    )
                    
                    val editorViewModel: EditorViewModel = viewModel(
                        factory = ViewModelFactory(useCases)
                    )

                    val trashViewModel: TrashViewModel = viewModel(
                        factory = ViewModelFactory(useCases)
                    )

                    NavGraph(
                        navController = navController,
                        notesViewModel = notesViewModel,
                        editorViewModel = editorViewModel,
                        trashViewModel = trashViewModel
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
