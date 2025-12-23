package com.mininotes.app.ui.trash

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mininotes.app.data.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onNavigateBack: () -> Unit
) {
    val deletedNotes by viewModel.deletedNotes.collectAsState()
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToManage by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToView by remember { mutableStateOf<NoteEntity?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (deletedNotes.isNotEmpty()) {
                        IconButton(onClick = { showEmptyTrashDialog = true }) {
                            Icon(Icons.Default.Delete, "Empty Trash")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (deletedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Trash is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deletedNotes, key = { it.id }) { note ->
                    TrashNoteItem(
                        note = note,
                        onClick = { noteToManage = note }
                    )
                }
            }
        }

        if (noteToManage != null) {
            TrashOptionsDialog(
                onRestore = {
                    viewModel.restoreNote(noteToManage!!)
                    noteToManage = null
                },
                onDelete = {
                    noteToDelete = noteToManage
                    noteToManage = null
                },
                onView = {
                    noteToView = noteToManage
                    noteToManage = null
                },
                onDismiss = { noteToManage = null }
            )
        }

        if (noteToView != null) {
            ViewNoteDialog(
                note = noteToView!!,
                onDismiss = { noteToView = null }
            )
        }

        if (noteToDelete != null) {
            PermanentDeleteDialog(
                onConfirm = {
                    viewModel.permanentlyDeleteNote(noteToDelete!!)
                    noteToDelete = null
                },
                onDismiss = { noteToDelete = null }
            )
        }

        if (showEmptyTrashDialog) {
            EmptyTrashDialog(
                onConfirm = {
                    viewModel.emptyTrash()
                    showEmptyTrashDialog = false
                },
                onDismiss = { showEmptyTrashDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashNoteItem(
    note: NoteEntity,
    onClick: () -> Unit
) {
    val daysUntilDeletion = note.deletedAt?.let {
        30 - TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it)
    } ?: 30

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Deleted ${formatTimestamp(note.deletedAt ?: note.updatedAt)} • Deletes in $daysUntilDeletion days",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TrashOptionsDialog(
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note Options") },
        text = { 
            Column {
                TextButton(onClick = onView, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("View Note", color = MaterialTheme.colorScheme.onSurface)
                }
                TextButton(onClick = onRestore, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Restore Note", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Delete Permanently", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ViewNoteDialog(
    note: NoteEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(note.title.ifEmpty { "Untitled" }) },
        text = { 
            Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text(note.content)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun PermanentDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Permanently?") },
        text = { Text("This action cannot be undone. The note will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete Forever", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyTrashDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Empty Trash?") },
        text = { Text("All notes in trash will be permanently deleted. This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Empty Trash", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
