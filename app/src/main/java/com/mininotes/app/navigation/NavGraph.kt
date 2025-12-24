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
    noteUseCases: com.mininotes.app.domain.usecase.NoteUseCases,
    themeRepository: com.mininotes.app.data.repository.ThemeRepository
) {
    // Create Factories
    val viewModelFactory = com.mininotes.app.ViewModelFactory(noteUseCases)
    val settingsViewModelFactory = com.mininotes.app.ui.settings.SettingsViewModelFactory(themeRepository)

    NavHost(
        navController = navController,
        startDestination = "notes"
    ) {
        composable("notes") {
             val notesViewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = viewModelFactory
            )
            
            NotesScreen(
                viewModel = notesViewModel,
                onNoteClick = { noteId ->
                    navController.navigate("editor/$noteId")
                },
                onNewChecklist = {
                    navController.navigate("editor/0?type=CHECKLIST")
                },
                onNewDrawing = {
                    navController.navigate("editor/0?type=DRAWING")
                },
                onOpenTrash = {
                    navController.navigate("trash")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("settings") {
            val settingsViewModel: com.mininotes.app.ui.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = settingsViewModelFactory
            )
            
            com.mininotes.app.ui.settings.SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
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
            
            val editorViewModel: EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = viewModelFactory
            )
            
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
             val trashViewModel: TrashViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = viewModelFactory
            )
            
            TrashScreen(
                viewModel = trashViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
