package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Note
import com.example.ui.components.OverlayPermissionBanner
import com.example.ui.theme.NoteStyles
import com.example.ui.viewmodel.NotesViewModel
import com.example.util.AdaptStatusBarColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteInKeepView by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    // Multi-Select Mode State (Keep style)
    var selectedNoteIds by remember { mutableStateOf(setOf<Long>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    // Real-time Permission Sync on App Resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back button when in selection mode
    BackHandler(enabled = isSelectionMode) {
        selectedNoteIds = emptySet()
    }

    if (noteInKeepView == null) {
        val currentHeaderColor = if (isSelectionMode) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
        AdaptStatusBarColor(headerColor = currentHeaderColor)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (isSelectionMode) {
                    // Contextual Action Top Bar for Multi-Select
                    TopAppBar(
                        title = {
                            Text(
                                text = "${selectedNoteIds.size} selected",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { selectedNoteIds = emptySet() },
                                modifier = Modifier.testTag("exit_selection_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Selection"
                                )
                            }
                        },
                        actions = {
                            // Select / Deselect All
                            IconButton(
                                onClick = {
                                    selectedNoteIds = if (selectedNoteIds.size == notes.size) {
                                        emptySet()
                                    } else {
                                        notes.map { it.id }.toSet()
                                    }
                                },
                                modifier = Modifier.testTag("select_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select All"
                                )
                            }

                            // Delete Selected Notes (Always confirms before deleting)
                            IconButton(
                                onClick = { showBulkDeleteDialog = true },
                                modifier = Modifier.testTag("bulk_delete_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                } else {
                    // Standard Home Top App Bar: No overlay active pill, no select button; has Guide '?' next to Settings
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.StickyNote2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Overlay Notes",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${notes.size} notes saved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            // Guide Button with '?' icon positioned right next to Settings
                            IconButton(
                                onClick = { showGuideDialog = true },
                                modifier = Modifier.testTag("main_guide_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "User Guide",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Settings Button
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.testTag("main_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            floatingActionButton = {
                if (!isSelectionMode) {
                    FloatingActionButton(
                        onClick = {
                            val preset = viewModel.getDefaultPreset()
                            noteToEdit = Note(
                                title = "",
                                content = "",
                                backgroundColor = preset.backgroundColor,
                                headerColor = preset.headerColor,
                                textColor = preset.textColor,
                                fontFamily = preset.fontFamily,
                                fontSize = preset.fontSize,
                                isBold = preset.isBold,
                                cornerRadius = preset.cornerRadius,
                                borderWidth = preset.borderWidth,
                                borderColor = preset.borderColor,
                                elevation = preset.elevation,
                                opacity = preset.opacity,
                                widthDp = preset.widthDp,
                                heightDp = preset.heightDp
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("create_note_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Note")
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search notes...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_notes_field")
                )

                // Overlay Permission Banner if needed
                OverlayPermissionBanner(
                    isPermissionGranted = isPermissionGranted,
                    onRefreshStatus = {
                        isPermissionGranted = Settings.canDrawOverlays(context)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Notes List / Grid (Filter chips row removed per request)
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NoteAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No notes match '$searchQuery'" else "No notes yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create your first floating note to keep your reminders always visible!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            val isSelected = note.id in selectedNoteIds
                            NoteCard(
                                note = note,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onOpen = {
                                    if (isSelectionMode) {
                                        selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                                    } else {
                                        noteInKeepView = note
                                    }
                                },
                                onLongClick = {
                                    selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                                },
                                onFloat = {
                                    if (Settings.canDrawOverlays(context)) {
                                        viewModel.floatNote(context, note)
                                    } else {
                                        showPermissionDialog = true
                                    }
                                },
                                onEdit = { noteToEdit = note },
                                onDelete = { noteToDelete = note },
                                onDuplicate = { viewModel.duplicateNote(note) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Pop-up Guide Dialog (Transferred from Settings)
    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Overlay Notes Guide",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Floating Controls & Gestures",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val gestureGuides = listOf(
                        Triple(Icons.Default.DragIndicator, "Drag Header", "Press and drag anywhere on the top bar to reposition the floating note on your screen."),
                        Triple(Icons.Default.Opacity, "Transparency", "Tap opacity icon to adjust transparency so you can see through your notes."),
                        Triple(Icons.Default.Remove, "Minimize to Bubble", "Tap '-' to shrink into a floating circular bubble on screen edge."),
                        Triple(Icons.Default.Lock, "Lock / Read-Only", "Locks drag and editing so you can read notes without accidental clicks."),
                        Triple(Icons.Default.NorthWest, "Resize Handle", "Drag the bottom-right corner to freely resize note dimensions."),
                        Triple(Icons.Default.MoreVert, "Compact Mode", "Enable in Settings to condense all buttons into a single 3-dots menu.")
                    )

                    gestureGuides.forEach { (icon, title, desc) ->
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGuideDialog = false },
                    modifier = Modifier.testTag("dismiss_guide_dialog_button")
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // Google Keep-style Fullscreen Note View / Editor
    noteInKeepView?.let { viewingNote ->
        NoteDetailScreen(
            note = viewingNote,
            onBack = { updated ->
                viewModel.saveNote(updated)
                noteInKeepView = null
            },
            onFloat = { target ->
                viewModel.saveNote(target)
                if (Settings.canDrawOverlays(context)) {
                    viewModel.floatNote(context, target)
                } else {
                    showPermissionDialog = true
                }
            },
            onOpenFullCustomize = { target ->
                noteInKeepView = null
                noteToEdit = target
            },
            onDelete = { target ->
                viewModel.deleteNote(target)
                noteInKeepView = null
            }
        )
    }

    // Note Editor Modal Bottom Sheet (Style Customizer)
    noteToEdit?.let { note ->
        NoteEditorSheet(
            note = note,
            onDismiss = { noteToEdit = null },
            onSave = { saved ->
                viewModel.saveNote(saved)
            }
        )
    }

    // Single Note Delete Confirmation Dialog
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to permanently delete \"${note.title.ifBlank { "Untitled Note" }}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(note)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_note_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bulk Delete Confirmation Dialog (Keep-style multi delete)
    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Delete ${selectedNoteIds.size} Notes?") },
            text = { Text("Are you sure you want to permanently delete the ${selectedNoteIds.size} selected notes? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = notes.filter { it.id in selectedNoteIds }
                        viewModel.deleteNotes(toDelete)
                        selectedNoteIds = emptySet()
                        showBulkDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_bulk_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Permission Prompt Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enable Overlay Permission")
                }
            },
            text = {
                Text("To display floating sticky notes on top of other applications, Android requires the \"Display over other apps\" permission.\n\nTap \"Open Settings\" to enable it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        try {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        } catch (e: Exception) {
                            context.startActivity(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            )
                        }
                    },
                    modifier = Modifier.testTag("dialog_open_settings_button")
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onOpen: () -> Unit,
    onLongClick: () -> Unit,
    onFloat: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val resolvedColors = remember(note.backgroundColor, note.headerColor, note.textColor, note.borderColor, isDark) {
        NoteStyles.resolveNoteColors(note, isDark)
    }
    val bgColor = resolvedColors.backgroundColor
    val headerColor = resolvedColors.headerColor
    val textColor = resolvedColors.textColor
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else resolvedColors.borderColor
    val cornerShape = remember(note.cornerRadius) { RoundedCornerShape(note.cornerRadius.dp) }
    val fontFamily = remember(note.fontFamily) { NoteStyles.resolveFontFamily(note.fontFamily) }
    val fontWeight = remember(note.isBold) { NoteStyles.resolveFontWeight(note.isBold) }

    Card(
        shape = cornerShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.5.dp else note.borderWidth.dp, borderColor, cornerShape)
            .clip(cornerShape)
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onLongClick
            )
            .testTag("note_card_${note.id}")
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Checkbox indicator when in selection mode
                    if (isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isSelectionMode) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showMenu) {
                            DropdownMenu(
                                expanded = true,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note") },
                                    leadingIcon = { Icon(Icons.Default.PushPin, null) },
                                    onClick = {
                                        showMenu = false
                                        onTogglePin()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicate()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Note Content Preview
            Text(
                text = note.content.ifBlank { "(No content yet - tap to write)" },
                fontFamily = fontFamily,
                fontSize = (note.fontSize.coerceIn(12, 16)).sp,
                fontWeight = fontWeight,
                color = textColor.copy(alpha = if (note.content.isBlank()) 0.4f else 0.9f),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
            )

            // Matching Float and Edit Pill Buttons
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Float Button Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = headerColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .clickable { onFloat() }
                            .testTag("float_note_button_${note.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPicture,
                                contentDescription = "Float Note",
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Float",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }

                    // Edit Button Pill (Matching the exact shape, padding & style of Float button)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = headerColor.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .clickable { onEdit() }
                            .testTag("edit_note_button_${note.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note",
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
