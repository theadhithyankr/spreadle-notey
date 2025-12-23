package com.mininotes.app.ui.notes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit
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
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Spreadle Notes",
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    NavigationDrawerItem(
                        label = { Text("Notes") },
                        icon = { Icon(Icons.Default.Lightbulb, null) },
                        selected = !uiState.showArchived,
                        onClick = {
                            if (uiState.showArchived) viewModel.toggleArchiveView()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Archive") },
                        icon = { Icon(Icons.Outlined.Archive, null) },
                        selected = uiState.showArchived,
                        onClick = {
                            if (!uiState.showArchived) viewModel.toggleArchiveView()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Trash") },
                        icon = { Icon(Icons.Outlined.Delete, null) },
                        selected = false,
                        onClick = {
                            onOpenTrash()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Default.Settings, null) },
                        selected = false,
                        onClick = {
                            onOpenSettings()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
    val inSelectionMode = uiState.selectedNoteIds.isNotEmpty()
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (inSelectionMode) {
                val selectedNotes = uiState.notes.filter { it.id in uiState.selectedNoteIds }
                val areAllPinned = selectedNotes.isNotEmpty() && selectedNotes.all { it.isPinned }
                
                SelectionTopAppBar(
                    selectedCount = uiState.selectedNoteIds.size,
                    areAllPinned = areAllPinned,
                    onClearSelection = { viewModel.clearSelection() },
                    onPin = { viewModel.pinSelectedNotes(!areAllPinned) }, 
                    onArchive = { viewModel.archiveSelectedNotes(!uiState.showArchived) }, 
                    onDelete = { 
                         noteToDelete = NoteEntity(id=-2, title="", content="dummy", updatedAt=0, createdAt=0) 
                    }
                )
                // Wait, onArchive logic above is WRONG. I need `viewModel.archiveSelectedNotes()`.
                // I don't have `archiveSelectedNotes` in ViewModel?
                // I defined `pinSelectedNotes` and `deleteSelectedNotes`.
                // I forgot `archiveSelectedNotes`?
                // Checking NotesViewModel again.
            } else {
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
                            selectedNoteIds = uiState.selectedNoteIds,
                            onNoteClick = { 
                                if (inSelectionMode) viewModel.toggleSelection(it) 
                                else onNoteClick(it) 
                            },
                            onNoteLongPress = { viewModel.startSelection(it) },
                            onTogglePin = { viewModel.togglePinNote(it) },
                            onToggleArchive = { viewModel.toggleArchiveNote(it) }
                        )
                    }
                }
            }
        },
        // Remove BottomBar
        floatingActionButton = {
            if (!inSelectionMode) {
                Column(horizontalAlignment = Alignment.End) {
                    if (fabExpanded) {
                        // ... (FAB content) ...
                        ExtendedFloatingActionButton(
                            onClick = { 
                                onNewChecklist()
                                fabExpanded = false 
                            },
                            icon = { Icon(Icons.Default.CheckBox, "New List") },
                            text = { Text("List") },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            expanded = true
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        ExtendedFloatingActionButton(
                            onClick = { 
                                viewModel.createNewNote(onNoteClick)
                                fabExpanded = false 
                            },
                            icon = { Icon(Icons.Default.Edit, "New Text Note") },
                            text = { Text("Text") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            expanded = true
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Create",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
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
                    selectedNoteIds = uiState.selectedNoteIds,
                    onNoteClick = { 
                        if (inSelectionMode) viewModel.toggleSelection(it) 
                        else onNoteClick(it) 
                    },
                    onNoteLongPress = { viewModel.startSelection(it) },
                    onTogglePin = { viewModel.togglePinNote(it) },
                    onToggleArchive = { viewModel.toggleArchiveNote(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        if (noteToDelete != null) {
            val isMultiDelete = noteToDelete?.id == -2L
            DeleteConfirmationDialog(
                onConfirm = {
                    if (isMultiDelete) {
                        viewModel.deleteSelectedNotes()
                    } else {
                        viewModel.moveToTrash(noteToDelete!!)
                    }
                    noteToDelete = null
                },
                onDismiss = { noteToDelete = null },
                isMulti = isMultiDelete
            )
        }
    }
}
}

@Composable
fun SmallFab(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
        }
        androidx.compose.material3.SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(icon, contentDescription = label)
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
    selectedNoteIds: Set<Long>,
    onNoteClick: (Long) -> Unit,
    onNoteLongPress: (Long) -> Unit,
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
            val isSelected = selectedNoteIds.contains(note.id)
            NoteItem(
                note = note,
                searchQuery = searchQuery,
                isSelected = isSelected,
                isInSelectionMode = selectedNoteIds.isNotEmpty(),
                onClick = { onNoteClick(note.id) },
                onLongPress = { onNoteLongPress(note.id) },
                onTogglePin = { onTogglePin(note) },
                onToggleArchive = { onToggleArchive(note) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    searchQuery: String,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit
) {
    // Keep Style: Outlined Card, specific colors
    val cardColor = note.color?.let { 
        try { Color(android.graphics.Color.parseColor(it)) } 
        catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.surface 
    
    // Highlight logic
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderStroke = if (isSelected) 3.dp else 1.dp
    
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ), 
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
        border = BorderStroke(borderStroke, borderColor)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
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
         
         if (note.isPinned) {
             Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = "Pinned",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(18.dp)
             )
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
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, isMulti: Boolean = false) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isMulti) "Delete Selected?" else "Delete Note") },
        text = { Text(if (isMulti) "Move selected notes to trash?" else "Move this note to trash?") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    areAllPinned: Boolean,
    onClearSelection: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("$selectedCount Selected", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onPin) {
                Icon(
                    if (areAllPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin, 
                    if (areAllPinned) "Unpin" else "Pin"
                )
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Outlined.Archive, "Archive")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, "Delete")
            }
        },
        colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
