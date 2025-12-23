package com.mininotes.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mininotes.app.data.NoteType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    noteId: Long,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Local State for Text Fields to prevent Cursor Jumping
    var titleTextFieldValue by remember { mutableStateOf(TextFieldValue(text = state.title)) }
    var contentTextFieldValue by remember { mutableStateOf(TextFieldValue(text = state.content)) }

    // Sync only when LOADING data (e.g. initial load)
    // We check if the VM text is significantly different (e.g. not just a type-ahead) 
    // or rely on a "loaded" flag. simpler:
    // If local text is empty and state text is not, sync. 
    // Or just one-time sync using LaunchedEffect.
    LaunchedEffect(state.title) {
        if (state.title != titleTextFieldValue.text) {
             titleTextFieldValue = titleTextFieldValue.copy(text = state.title)
        }
    }
    LaunchedEffect(state.content) {
         if (state.content != contentTextFieldValue.text) {
             contentTextFieldValue = contentTextFieldValue.copy(text = state.content)
        }
    }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    
    // Auto-save on back
    BackHandler {
        viewModel.saveNote()
        onBackClick()
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveNote(); onBackClick() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateState { it.copy(isPinned = !state.isPinned) } }) {
                        Icon(if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "Pin")
                    }
                    IconButton(onClick = { viewModel.updateState { it.copy(isArchived = !state.isArchived) } }) {
                        Icon(if (state.isArchived) Icons.Filled.Archive else Icons.Outlined.Archive, "Archive")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { 
                                viewModel.updateState { it.copy(isDeleted = true) }
                                viewModel.saveNote() // Save then exit
                                onBackClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (state.type == NoteType.CHECKLIST) "Show Checkboxes" else "Hide Checkboxes") },
                            leadingIcon = { Icon(Icons.Default.CheckBox, null) },
                            onClick = {
                                viewModel.toggleNoteType()
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .imePadding()
        ) {
            // Title Input
            BasicTextField(
                value = titleTextFieldValue,
                onValueChange = { 
                    titleTextFieldValue = it
                    viewModel.updateTitle(it.text)
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (titleTextFieldValue.text.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (state.type == NoteType.CHECKLIST) {
                // Checklist
                Column {
                    state.checklistItems.forEachIndexed { index, item ->
                        ChecklistItemRow(
                            item = item,
                            onCheckedChange = { viewModel.toggleChecklistItem(index) },
                            onTextChange = { viewModel.updateChecklistItem(index, it) },
                            onDelete = { viewModel.removeChecklistItem(index) }
                        )
                    }
                    
                    // Add Item Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.addChecklistItem() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text("List item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Main Content Input
                BasicTextField(
                    value = contentTextFieldValue,
                    onValueChange = { 
                        contentTextFieldValue = it
                        viewModel.updateContent(it.text)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (contentTextFieldValue.text.isEmpty()) {
                            Text(
                                text = "Note",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: com.mininotes.app.data.ChecklistItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        // Local state for list items too? Ideally yes, but list reordering makes it tricky.
        // For checklist items, simple string update is usually fine as they are short.
        // If cursor jump happens here, we need a separate component with local state.
        // Let's implement a simple wrapper if needed. For now, strict BasicTextField.
        
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
