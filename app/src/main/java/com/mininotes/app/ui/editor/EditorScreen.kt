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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Brush
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mininotes.app.data.NoteType
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    noteId: Long,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Local State using noteId as key to RESET when switching notes
    var titleTextFieldValue by remember(noteId) { 
        mutableStateOf(TextFieldValue(text = if (state.noteId == noteId) state.title else "")) 
    }
    var contentTextFieldValue by remember(noteId) { 
        mutableStateOf(TextFieldValue(text = if (state.noteId == noteId) state.content else "")) 
    }

    // Sync state
    LaunchedEffect(state.title) {
        if (state.title != titleTextFieldValue.text) { titleTextFieldValue = TextFieldValue(text = state.title) }
    }
    LaunchedEffect(state.content) {
         if (state.content != contentTextFieldValue.text) { contentTextFieldValue = TextFieldValue(text = state.content) }
    }

    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    
    BackHandler {
        viewModel.saveNote()
        onBackClick()
    }

    val contentFocusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Focus management for checklist
    var focusIndex by remember { mutableStateOf(-1) }
    
    var showMenu by remember { mutableStateOf(false) }

    if (state.type == NoteType.DRAWING) {
        DrawingEditor(
            initialFilePath = state.content,
            onSave = { path ->
                viewModel.updateContent(path)
                viewModel.saveNote()
            },
            onBack = onBackClick
        )
        return
    }

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
                            text = { Text(if (state.type == NoteType.CHECKLIST) "Show Text" else "Show Checkboxes") },
                            leadingIcon = { Icon(Icons.Default.CheckBox, null) },
                            onClick = {
                                viewModel.toggleNoteType()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grocery Mode") },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                            onClick = {
                                viewModel.setNoteType(NoteType.GROCERY)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Drawing") },
                            leadingIcon = { Icon(Icons.Default.Brush, null) },
                            onClick = {
                                viewModel.setNoteType(NoteType.DRAWING)
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
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { contentFocusRequester.requestFocus() } 
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

            if (state.type == NoteType.CHECKLIST || state.type == NoteType.GROCERY) {
                // Checklist or Grocery List
                Column {
                    state.checklistItems.forEachIndexed { index, item ->
                        val itemFocusRequester = remember { FocusRequester() }
                        
                        LaunchedEffect(focusIndex) {
                            if (focusIndex == index) {
                                itemFocusRequester.requestFocus()
                                focusIndex = -1 // Reset after focusing
                            }
                        }

                        if (state.type == NoteType.GROCERY) {
                             GroceryItemRow(
                                item = item,
                                focusRequester = itemFocusRequester,
                                onCheckedChange = { viewModel.toggleChecklistItem(index) },
                                onTextChange = { 
                                    if (it.contains("\n")) {
                                        val safeText = it.replace("\n", "")
                                        viewModel.updateChecklistItem(index, safeText)
                                        viewModel.addChecklistItem(index + 1, "")
                                        focusIndex = index + 1
                                    } else {
                                        viewModel.updateChecklistItem(index, it) 
                                    }
                                },
                                onDetailsChange = { q, u, p, c ->  viewModel.updateGroceryItem(index, q, u, p, c) },
                                onDelete = { viewModel.removeChecklistItem(index) }
                            )
                        } else {
                            ChecklistItemRow(
                                item = item,
                                focusRequester = itemFocusRequester,
                                onCheckedChange = { viewModel.toggleChecklistItem(index) },
                                onTextChange = { 
                                    if (it.contains("\n")) {
                                        val safeText = it.replace("\n", "")
                                        viewModel.updateChecklistItem(index, safeText)
                                        viewModel.addChecklistItem(index + 1, "")
                                        focusIndex = index + 1
                                    } else {
                                        viewModel.updateChecklistItem(index, it) 
                                    }
                                },
                                onDelete = { viewModel.removeChecklistItem(index) }
                            )
                        }
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
                        Text(if(state.type == NoteType.GROCERY) "Add Item" else "List item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    if (state.type == NoteType.GROCERY) {
                        // Calculate Total Price (Simple sum)
                         val total = state.checklistItems.sumOf { 
                             try { (it.price?.toDouble() ?: 0.0) * (it.quantity?.toDouble() ?: 1.0) } catch(e: Exception) { 0.0 } 
                         }
                         if (total > 0) {
                             Spacer(Modifier.height(16.dp))
                             Text("Total Estimated Cost: $${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                         }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(contentFocusRequester)
                )
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: com.mininotes.app.data.ChecklistItem,
    focusRequester: FocusRequester,
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
        
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .focusRequester(focusRequester)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GroceryItemRow(
    item: com.mininotes.app.data.ChecklistItem,
    focusRequester: FocusRequester,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDetailsChange: (String?, String?, String?, String?) -> Unit,
    onDelete: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Top Row: Checkbox + Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            BasicTextField(
                value = item.text,
                onValueChange = onTextChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focusRequester)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        // Details Row: Qty, Unit, Price
        if (!item.isChecked) { // Hide details if checked/done to save space
            Row(
                Modifier.fillMaxWidth().padding(start = 50.dp, end = 16.dp, bottom = 4.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Qty
                 SmallTextField(
                     value = item.quantity ?: "",
                     placeholder = "Qty",
                     onValueChange = { onDetailsChange(it, item.unit, item.price, item.category) },
                     modifier = Modifier.width(60.dp).padding(end = 8.dp)
                 )
                 // Unit (Simple Text for now, or dropdown)
                 SmallTextField(
                     value = item.unit ?: "",
                     placeholder = "Unit",
                     onValueChange = { onDetailsChange(item.quantity, it, item.price, item.category) },
                     modifier = Modifier.width(60.dp).padding(end = 8.dp)
                 )
                 // Price
                 SmallTextField(
                     value = item.price ?: "",
                     placeholder = "$",
                     onValueChange = { onDetailsChange(item.quantity, item.unit, it, item.category) },
                     modifier = Modifier.weight(1f)
                 )
            }
        }
    }
}



@Composable
fun SmallTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
             if (value.isEmpty()) {
                 Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
             }
             innerTextField()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .padding(4.dp)
    )
}
