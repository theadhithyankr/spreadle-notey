package com.mininotes.app.ui.notes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mininotes.app.data.NoteEntity
import com.mininotes.app.data.NoteType
import com.mininotes.app.data.SortOrder
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onNewChecklist: () -> Unit,
    onOpenTrash: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) } // Toggle layout
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.padding(16.dp))
                Text("Mini Notes", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
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
                // Keep-style Floating Search Bar (Only when NOT searching active)
                if (!searchActive) {
                    KeepSearchBarHeader(
                        title = if (uiState.showArchived) "Archive" else "Search your notes",
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { searchActive = true },
                        onViewToggle = { isGridView = !isGridView },
                        isGridView = isGridView
                    )
                } else {
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
                                Icon(Icons.Default.Close, "Close") 
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NotesGrid(
                            notes = uiState.notes,
                            searchQuery = uiState.searchQuery,
                            isGridView = isGridView,
                            onNoteClick = onNoteClick,
                            onNoteLongPress = { noteToDelete = it },
                            onTogglePin = { viewModel.togglePinNote(it) },
                            onToggleArchive = { viewModel.toggleArchiveNote(it) }
                        )
                    }
                }
            },
            bottomBar = {
                KeepBottomBar(
                    onNewList = onNewChecklist
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.createNewNote(onNoteClick) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // Keepish color
                    // contentColor matches
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Note", modifier = Modifier.size(36.dp))
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
                        isGridView = isGridView,
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
fun KeepSearchBarHeader(
    title: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onViewToggle: () -> Unit,
    isGridView: Boolean
) {
    // Keep Style: Floating Pill below status bar
    // We use a Box container to handle the padding and offsets
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // Ensure opaque behind stats bar
            .statusBarsPadding() // Handle status bar inset
            .padding(horizontal = 16.dp, vertical = 8.dp) // Spacing around the pill
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp) // standard pill height
                .clickable { onSearchClick() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu")
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                IconButton(onClick = onViewToggle) {
                    Icon(if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView, "View")
                }
            }
        }
    }
}

@Composable
fun KeepBottomBar(
    onNewList: () -> Unit
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onNewList) {
                Icon(Icons.Default.CheckBox, "New List")
            }
        },
        floatingActionButton = {} // FAB is handled by Scaffold to dock correctly
    )
}

@Composable
fun NotesGrid(
    notes: List<NoteEntity>,
    searchQuery: String,
    isGridView: Boolean,
    onNoteClick: (Long) -> Unit,
    onNoteLongPress: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleArchive: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = if (isGridView) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1),
        contentPadding = PaddingValues(12.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    searchQuery: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit
) {
    // Keep Style: Outlined Card, specific colors
    val cardColor = note.color?.let { 
        try { Color(android.graphics.Color.parseColor(it)) } 
        catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.surface // Default to surface (dark)
    
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Long press handled via combinedClickable if needed, for now standard clickable
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Content
            if (note.title.isNotEmpty()) {
                Text(
                    text = highlightSearchQuery(note.title, searchQuery),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            
            if (note.type == NoteType.CHECKLIST) {
                // Preview first 5 items
                note.checklistItems.take(5).forEach { item ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                        Icon(
                            if (item.isChecked) Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp),
                            tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface 
                        )
                    }
                }
                if (note.checklistItems.size > 5) {
                     Text("+ ${note.checklistItems.size - 5} checked items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                 if (note.content.isNotEmpty()) {
                    Text(
                        text = highlightSearchQuery(note.content, searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }

            // Labels/Chips (Keep style pill)
             if (note.labels.isNotEmpty()) {
                 Spacer(Modifier.size(12.dp))
                 Row {
                     note.labels.split(",").take(3).forEach { label ->
                          Surface(
                              shape = CircleShape,
                              color = MaterialTheme.colorScheme.surfaceVariant,
                              modifier = Modifier.padding(end = 4.dp)
                          ) {
                               Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                   Icon(Icons.Filled.PushPin, null, Modifier.size(10.dp)) // Generic icon for label
                                   Spacer(Modifier.width(4.dp))
                                   Text(text = label, style = MaterialTheme.typography.labelSmall)
                               }
                          }
                     }
                 }
             }
        }
    }
}

// Helpers
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
             style = MaterialTheme.typography.bodyLarge,
             color = MaterialTheme.colorScheme.onSurfaceVariant
         )
    }
}
