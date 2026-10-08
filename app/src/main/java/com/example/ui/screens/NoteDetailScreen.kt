package com.example.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.theme.NoteStyles
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    note: Note,
    onBack: (Note) -> Unit,
    onFloat: (Note) -> Unit,
    onOpenFullCustomize: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentNote by remember { mutableStateOf(note) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val resolvedColors = NoteStyles.resolveNoteColors(currentNote, isDark)
    val bgColor = resolvedColors.backgroundColor
    val headerColor = resolvedColors.headerColor
    val textColor = resolvedColors.textColor
    val fontFamily = NoteStyles.resolveFontFamily(currentNote.fontFamily)
    val fontWeight = NoteStyles.resolveFontWeight(currentNote.isBold)

    // Dynamically adapt Android system status bar color to match headerColor
    com.example.util.AdaptStatusBarColor(headerColor = headerColor)

    val scrollState = rememberScrollState()
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(currentNote.content) {
        if (scrollState.value >= scrollState.maxValue - 250) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    BackHandler {
        onBack(currentNote)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Save on exit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
    ) {
        // Status bar & Top App Bar filled with headerColor
        Surface(
            color = headerColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            TopAppBar(
                title = {},
                windowInsets = TopAppBarDefaults.windowInsets,
                navigationIcon = {
                    IconButton(
                        onClick = { onBack(currentNote) },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // Pin button
                    IconButton(
                        onClick = {
                            val updated = currentNote.copy(isPinned = !currentNote.isPinned)
                            currentNote = updated
                        },
                        modifier = Modifier.testTag("detail_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (currentNote.isPinned) "Unpin Note" else "Pin Note",
                            tint = if (currentNote.isPinned) textColor else textColor.copy(alpha = 0.5f)
                        )
                    }

                    // Float Button
                    IconButton(
                        onClick = {
                            onFloat(currentNote)
                        },
                        modifier = Modifier.testTag("detail_float_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Float Over Apps",
                            tint = textColor
                        )
                    }

                    // Customize / Style Button
                    IconButton(
                        onClick = {
                            onOpenFullCustomize(currentNote)
                        },
                        modifier = Modifier.testTag("detail_customize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Customize Style",
                            tint = textColor
                        )
                    }

                    // Delete Button (Opens confirmation dialog)
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = textColor.copy(alpha = 0.75f)
                        )
                    }

                    // Save Icon Button at top right corner
                    IconButton(
                        onClick = { onBack(currentNote) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("detail_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Note",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerColor
                )
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note?") },
                text = { Text("Are you sure you want to permanently delete \"${currentNote.title.ifBlank { "Untitled Note" }}\"? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(currentNote)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("confirm_delete_detail_button")
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Note Body (Title + Unlimited Content)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Title Field (Borderless, clean like Keep)
            BasicTextField(
                value = currentNote.title,
                onValueChange = { currentNote = currentNote.copy(title = it, updatedAt = System.currentTimeMillis()) },
                textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                cursorBrush = SolidColor(textColor),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (currentNote.title.isEmpty()) {
                            Text(
                                text = "Title",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.45f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_title_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Field (Borderless, unlimited lines like Keep)
            BasicTextField(
                value = currentNote.content,
                onValueChange = { currentNote = currentNote.copy(content = it, updatedAt = System.currentTimeMillis()) },
                textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = (currentNote.fontSize.coerceIn(14, 22)).sp,
                    fontWeight = fontWeight,
                    color = textColor,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(textColor),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (currentNote.content.isEmpty()) {
                            Text(
                                text = "Note",
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontSize = (currentNote.fontSize.coerceIn(14, 22)).sp,
                                    fontWeight = fontWeight,
                                    color = textColor.copy(alpha = 0.45f),
                                    lineHeight = 24.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_content_field")
            )

            Spacer(modifier = Modifier.height(180.dp))
        }

        // Bottom Info Bar (Timestamp & Metadata)
        Surface(
            color = headerColor.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault())
                val lastEdited = dateFormat.format(Date(currentNote.updatedAt))

                Text(
                    text = "Edited $lastEdited",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.65f)
                    )
                )

                val wordCount = if (currentNote.content.isBlank()) 0 else currentNote.content.trim().split("\\s+".toRegex()).size
                val charCount = currentNote.content.length

                Text(
                    text = "$wordCount words • $charCount chars",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.65f)
                    )
                )
            }
        }
    }
}
