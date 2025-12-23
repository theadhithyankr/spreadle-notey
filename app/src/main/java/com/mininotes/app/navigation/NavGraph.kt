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
    editorViewModel: EditorViewModel,
    trashViewModel: TrashViewModel
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
                onOpenTrash = {
                    navController.navigate("trash")
                }
            )
        }
        
        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
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
