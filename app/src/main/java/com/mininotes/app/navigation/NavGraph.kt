package com.mininotes.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mininotes.app.ui.editor.EditorScreen
import com.mininotes.app.ui.editor.EditorViewModel
import com.mininotes.app.ui.notes.NotesScreen
import com.mininotes.app.ui.notes.NotesViewModel
import com.mininotes.app.ui.trash.TrashScreen
import com.mininotes.app.ui.trash.TrashViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    notesViewModel: NotesViewModel,
    trashViewModel: TrashViewModel,
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
) {
    NavHost(
        navController = navController,
        startDestination = "notes"
    ) {
        composable("notes") {
            NotesScreen(
                viewModel = notesViewModel,
                onNoteClick = { noteId ->
                    navController.navigate("editor/$noteId")
                },
                onNewChecklist = {
                    navController.navigate("editor/0?type=CHECKLIST")
                },
                onOpenTrash = {
                    navController.navigate("trash")
                }
            )
        }
        
        composable(
            route = "editor/{noteId}?type={type}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType; defaultValue = "TEXT" }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val typeStr = backStackEntry.arguments?.getString("type") ?: "TEXT"
            val initialType = try {
                 com.mininotes.app.data.NoteType.valueOf(typeStr)
            } catch (e: Exception) { com.mininotes.app.data.NoteType.TEXT }
            
            // Create ViewModel scoped to this BackStackEntry
            val editorViewModel: EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = viewModelFactory
            )
            
            // Pass initial type to VM if new note
            if (noteId == 0L) {
                 editorViewModel.setInitialType(initialType)
            }
            
            EditorScreen(
                viewModel = editorViewModel,
                noteId = noteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("trash") {
            TrashScreen(
                viewModel = trashViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
