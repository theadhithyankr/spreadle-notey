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
import com.mininotes.app.data.NoteDao
import com.mininotes.app.data.NotesDatabase
import com.mininotes.app.navigation.NavGraph
import com.mininotes.app.ui.editor.EditorViewModel
import com.mininotes.app.ui.notes.NotesViewModel
import com.mininotes.app.ui.theme.MiniNotesTheme

class MainActivity : ComponentActivity() {
    private lateinit var noteDao: NoteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NotesDatabase.getDatabase(applicationContext)
        noteDao = database.noteDao()

        setContent {
            MiniNotesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    val notesViewModel: NotesViewModel = viewModel(
                        factory = ViewModelFactory(noteDao)
                    )
                    
                    val editorViewModel: EditorViewModel = viewModel(
                        factory = ViewModelFactory(noteDao)
                    )

                    NavGraph(
                        navController = navController,
                        notesViewModel = notesViewModel,
                        editorViewModel = editorViewModel
                    )
                }
            }
        }
    }
}

class ViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(noteDao) as T
            }
            modelClass.isAssignableFrom(EditorViewModel::class.java) -> {
                EditorViewModel(noteDao) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
