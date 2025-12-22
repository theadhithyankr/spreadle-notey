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

sealed class Screen(val route: String) {
    data object Notes : Screen("notes")
    data object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: Long) = "editor/$noteId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    notesViewModel: NotesViewModel,
    editorViewModel: EditorViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                viewModel = notesViewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor.createRoute(noteId))
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            EditorScreen(
                noteId = noteId,
                viewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
