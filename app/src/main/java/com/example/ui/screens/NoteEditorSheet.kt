package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.data.model.Note
import com.example.ui.components.ColorPickerSection
import com.example.ui.components.FontTypographySection
import com.example.ui.components.ShapeStyleSection
import com.example.ui.overlay.OverlayNoteWindow
import com.example.util.AdaptStatusBarColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(
    note: Note,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val customFonts = remember { prefsManager.getCustomFonts() }

    var editedNote by remember { mutableStateOf(note) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = null,
        modifier = Modifier.testTag("note_editor_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header Top Bar stays standard theme color (does not change with overlay color)
            AdaptStatusBarColor(headerColor = MaterialTheme.colorScheme.surface)

            Surface(
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (note.id == 0L) "New Note" else "Edit Note",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("editor_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                onSave(editedNote)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .height(38.dp)
                                .testTag("editor_save_button")
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Live Preview (Non-interactive visual preview)
            if (selectedTab != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(vertical = 10.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LIVE PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Visual only preview - touch blocked to prevent interaction
                        Box(contentAlignment = Alignment.Center) {
                            OverlayNoteWindow(
                                note = editedNote,
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(175.dp)
                            )
                            // Transparent interceptor layer
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(enabled = false) {}
                            )
                        }
                    }
                }
            }

            // Navigation Tabs (Content, Color, Font, Style)
            val tabs = listOf("Content", "Color", "Font", "Style")
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        modifier = Modifier.testTag("editor_tab_$index")
                    )
                }
            }

            // Tab Content Body
            if (selectedTab == 0) {
                // Tab 0: Writing area naturally fills all available vertical space
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    OutlinedTextField(
                        value = editedNote.title,
                        onValueChange = { editedNote = editedNote.copy(title = it) },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Quick Memo, Study Notes") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Content Field fills the remaining vertical space naturally
                    OutlinedTextField(
                        value = editedNote.content,
                        onValueChange = { editedNote = editedNote.copy(content = it) },
                        label = { Text("Content / Notes") },
                        placeholder = { Text("Write your notes, todo items, or reminders...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_content_input")
                    )
                }
            } else {
                // Tabs 1, 2, 3: Scrollable customization settings
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    when (selectedTab) {
                        1 -> {
                            // Color Customization with custom color palette
                            ColorPickerSection(
                                selectedBgColor = editedNote.backgroundColor,
                                onColorPresetSelected = { preset ->
                                    editedNote = editedNote.copy(
                                        backgroundColor = preset.backgroundColor,
                                        headerColor = preset.headerColor,
                                        textColor = preset.textColor,
                                        borderColor = preset.borderColor
                                    )
                                },
                                onCustomBgColorSelected = { colorLong ->
                                    editedNote = editedNote.copy(
                                        backgroundColor = colorLong,
                                        headerColor = colorLong
                                    )
                                }
                            )
                        }

                        2 -> {
                            // Typography Customization with Custom Fonts support
                            FontTypographySection(
                                selectedFontFamily = editedNote.fontFamily,
                                fontSize = editedNote.fontSize,
                                isBold = editedNote.isBold,
                                onFontFamilyChange = { editedNote = editedNote.copy(fontFamily = it) },
                                onFontSizeChange = { editedNote = editedNote.copy(fontSize = it) },
                                onBoldChange = { editedNote = editedNote.copy(isBold = it) },
                                customFonts = customFonts
                            )
                        }

                        3 -> {
                            // Style (Corner Radius, Borders & Initial Dimensions)
                            ShapeStyleSection(
                                cornerRadius = editedNote.cornerRadius,
                                borderWidth = editedNote.borderWidth,
                                opacity = editedNote.opacity,
                                onCornerRadiusChange = { editedNote = editedNote.copy(cornerRadius = it) },
                                onBorderWidthChange = { editedNote = editedNote.copy(borderWidth = it) },
                                onOpacityChange = { editedNote = editedNote.copy(opacity = it) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Initial Floating Window Dimensions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Width
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Initial Width: ${editedNote.widthDp}dp", style = MaterialTheme.typography.bodyMedium)
                            }
                            Slider(
                                value = editedNote.widthDp.toFloat(),
                                onValueChange = { editedNote = editedNote.copy(widthDp = it.roundToInt()) },
                                valueRange = 125f..380f,
                                modifier = Modifier.fillMaxWidth().testTag("width_dp_slider")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Height
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Initial Height: ${editedNote.heightDp}dp", style = MaterialTheme.typography.bodyMedium)
                            }
                            Slider(
                                value = editedNote.heightDp.toFloat(),
                                onValueChange = { editedNote = editedNote.copy(heightDp = it.roundToInt()) },
                                valueRange = 100f..460f,
                                modifier = Modifier.fillMaxWidth().testTag("height_dp_slider")
                            )
                        }
                    }
                }
            }
        }
    }
}
