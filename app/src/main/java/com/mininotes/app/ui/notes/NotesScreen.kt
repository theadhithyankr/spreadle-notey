package com.mininotes.app.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.NoteType
import com.mininotes.app.data.SortOrder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onOpenTrash: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) } // Default false to show grid first
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Notes") },
                    selected = !uiState.showArchived,
                    onClick = {
                        if (uiState.showArchived) viewModel.toggleArchiveView()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Archive") },
                    selected = uiState.showArchived,
                    onClick = {
                        if (!uiState.showArchived) viewModel.toggleArchiveView()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Trash") },
                    selected = false,
                    onClick = {
                        onOpenTrash()
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (searchActive) {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onSearch = { searchActive = false },
                        active = true,
                        onActiveChange = { if (!it) searchActive = false },
                        placeholder = { Text("Search notes...") },
                        leadingIcon = {
                            IconButton(onClick = { 
                                searchActive = false 
                                viewModel.updateSearchQuery("") // Clear search
                            }) { 
                                Icon(Icons.Default.Close, "Close Search") 
                            }
                        },
                        trailingIcon = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Results in search bar
                        NotesGrid(
                           notes = uiState.notes,
                           searchQuery = uiState.searchQuery,
                           onNoteClick = onNoteClick,
                           onNoteLongPress = { noteToDelete = it },
                           onTogglePin = { viewModel.togglePinNote(it) },
                           onToggleArchive = { viewModel.toggleArchiveNote(it) }
                        )
                    }
                } else {
                    TopAppBar(
                        title = { 
                            Text(if (uiState.showArchived) "Archive" else "Mini Notes")
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchActive = true }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                // Sort Options
                                SortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(order.name.replace("_", " ").lowercase(Locale.getDefault())
                                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) 
                                        },
                                        onClick = {
                                            viewModel.updateSortOrder(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.createNewNote(onNoteClick) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Note")
                }
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                if (uiState.notes.isEmpty()) {
                    EmptyNotesView(
                        isArchived = uiState.showArchived,
                        isSearching = uiState.searchQuery.isNotEmpty(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    NotesGrid(
                        notes = uiState.notes,
                        searchQuery = uiState.searchQuery,
                        onNoteClick = onNoteClick,
                        onNoteLongPress = { noteToDelete = it },
                        onTogglePin = { viewModel.togglePinNote(it) },
                        onToggleArchive = { viewModel.toggleArchiveNote(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (noteToDelete != null) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        viewModel.moveToTrash(noteToDelete!!)
                        noteToDelete = null
                    },
                    onDismiss = { noteToDelete = null }
                )
            }
        }
    }
}

@Composable
fun NotesGrid(
    notes: List<NoteEntity>,
    searchQuery: String,
    onNoteClick: (Long) -> Unit,
    onNoteLongPress: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleArchive: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                searchQuery = searchQuery,
                onClick = { onNoteClick(note.id) },
                onLongPress = { onNoteLongPress(note) },
                onTogglePin = { onTogglePin(note) },
                onToggleArchive = { onToggleArchive(note) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    searchQuery: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit
) {
    val backgroundColor = note.color?.let { 
        try { Color(android.graphics.Color.parseColor(it)) } 
        catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.surfaceVariant // Keep-like default
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (backgroundColor == MaterialTheme.colorScheme.surfaceVariant) null else null // Add border if default?
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Content
            if (note.title.isNotEmpty()) {
                Text(
                    text = highlightSearchQuery(note.title, searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
            
            if (note.type == NoteType.CHECKLIST) {
                // Preview first 4 items
                note.checklistItems.take(4).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.isChecked) Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp), // slightly larger
                            tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (note.checklistItems.size > 4) {
                     Text("+ ${note.checklistItems.size - 4} more", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                 if (note.content.isNotEmpty()) {
                    Text(
                        text = highlightSearchQuery(note.content, searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Labels
            if (note.labels.isNotEmpty()) {
                Spacer(Modifier.size(8.dp))
                Row {
                    note.labels.split(",").take(3).forEach { label ->
                         Text(
                            text = label, 
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}

// Helpers (Highlight, Dates, etc) - Reused/Simplified
@Composable
fun highlightSearchQuery(text: String, query: String) = buildAnnotatedString {
    if (query.isEmpty()) {
        append(text)
    } else {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        
        while (currentIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, currentIndex)
            if (index == -1) {
                append(text.substring(currentIndex))
                break
            }
            
            append(text.substring(currentIndex, index))
            withStyle(
                SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            currentIndex = index + query.length
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Note") },
        text = { Text("Move this note to trash?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmptyNotesView(isArchived: Boolean, isSearching: Boolean, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
         Text(
             if (isSearching) "No matches found" 
             else if (isArchived) "Archive is empty" 
             else "Notes you add appear here",
             style = MaterialTheme.typography.bodyLarge
         )
    }
}
