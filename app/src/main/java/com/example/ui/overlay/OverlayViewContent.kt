package com.example.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.local.PreferencesManager
import com.example.data.model.Note
import com.example.ui.theme.NoteStyles
import kotlin.math.roundToInt

/**
 * Custom TextToolbar implementation to render a floating context menu for BasicTextField
 */
class OverlayTextToolbar(
    private val onShow: (
        rect: Rect,
        onCopy: (() -> Unit)?,
        onPaste: (() -> Unit)?,
        onCut: (() -> Unit)?,
        onSelectAll: (() -> Unit)?
    ) -> Unit,
    private val onHide: () -> Unit
) : TextToolbar {
    private var _status: TextToolbarStatus = TextToolbarStatus.Hidden

    override val status: TextToolbarStatus
        get() = _status

    override fun hide() {
        _status = TextToolbarStatus.Hidden
        onHide()
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        _status = TextToolbarStatus.Shown
        onShow(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
    }
}

data class OverlayTextMenuState(
    val rect: Rect,
    val onCopy: (() -> Unit)?,
    val onPaste: (() -> Unit)?,
    val onCut: (() -> Unit)?,
    val onSelectAll: (() -> Unit)?
)

@Composable
fun OverlayNoteWindow(
    note: Note,
    modifier: Modifier = Modifier,
    headerScale: Float? = null,
    buttonSpacing: Int? = null,
    isCompactMode: Boolean? = null,
    showLock: Boolean? = null,
    showOpacity: Boolean? = null,
    showMinimize: Boolean? = null,
    showClose: Boolean? = null,
    onContentChange: (String) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onDragDelta: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onResizeDelta: (dw: Float, dh: Float) -> Unit = { _, _ -> },
    onOpacityChange: (Float) -> Unit = {},
    onToggleLock: () -> Unit = {},
    onToggleMinimize: () -> Unit = {},
    onClose: () -> Unit = {},
    onRequestFocus: (Boolean) -> Unit = {},
    onInteract: () -> Unit = {},
    isFloatingWindowManager: Boolean = false
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val prefsManager = remember { PreferencesManager(context) }

    // Dynamic settings with live override support for Settings preview
    val actualHeaderScale = headerScale ?: remember { prefsManager.getOverlayHeaderScale() }
    val actualButtonSpacing = buttonSpacing ?: remember { prefsManager.getOverlayButtonSpacing() }
    val actualIsCompactMode = isCompactMode ?: remember { prefsManager.isOverlayCompactMode() }
    val actualShowLock = showLock ?: remember { prefsManager.isOverlayShowLock() }
    val actualShowOpacity = showOpacity ?: remember { prefsManager.isOverlayShowOpacity() }
    val actualShowMinimize = showMinimize ?: remember { prefsManager.isOverlayShowMinimize() }
    val actualShowClose = showClose ?: remember { prefsManager.isOverlayShowClose() }

    var showOpacitySlider by remember { mutableStateOf(false) }
    var isContentFocused by remember { mutableStateOf(false) }
    var showCompactMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val contentScrollState = rememberScrollState()

    var textFieldValue by remember(note.id) {
        mutableStateOf(TextFieldValue(text = note.content, selection = TextRange(note.content.length)))
    }

    LaunchedEffect(note.content) {
        if (note.content != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = note.content,
                selection = TextRange(
                    textFieldValue.selection.start.coerceAtMost(note.content.length),
                    textFieldValue.selection.end.coerceAtMost(note.content.length)
                )
            )
        }
    }

    var textMenuState by remember { mutableStateOf<OverlayTextMenuState?>(null) }
    val overlayTextToolbar = remember {
        OverlayTextToolbar(
            onShow = { rect, onCopy, onPaste, onCut, onSelectAll ->
                textMenuState = OverlayTextMenuState(rect, onCopy, onPaste, onCut, onSelectAll)
            },
            onHide = {
                textMenuState = null
            }
        )
    }

    val isDark = isSystemInDarkTheme()
    val resolved = NoteStyles.resolveNoteColors(note, isDark)

    val bgColor = resolved.backgroundColor.copy(alpha = note.opacity)
    val headerColor = resolved.headerColor.copy(alpha = note.opacity)
    val textColor = resolved.textColor
    val borderColor = resolved.borderColor
    val cornerShape = RoundedCornerShape(note.cornerRadius.dp)
    val fontFamily = NoteStyles.resolveFontFamily(note.fontFamily)
    val fontWeight = NoteStyles.resolveFontWeight(note.isBold)

    if (note.isMinimized) {
        // Floating Bubble View
        FloatingBubbleView(
            note = note,
            onExpand = {
                onToggleMinimize()
                onInteract()
            },
            onDragDelta = { dx, dy ->
                onDragDelta(dx, dy)
                onInteract()
            },
            onInteract = onInteract,
            modifier = modifier
        )
        return
    }

    val actionButtonSize = (28 * actualHeaderScale).coerceIn(18f, 36f).dp
    val actionIconSize = (16 * actualHeaderScale).coerceIn(11f, 22f).dp
    val headerVerticalPad = (7 * actualHeaderScale).coerceIn(4f, 12f).dp

    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(bgColor)
            .border(
                width = note.borderWidth.dp,
                color = borderColor,
                shape = cornerShape
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .pointerInput(note.isLocked) {
                        if (!note.isLocked) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                focusManager.clearFocus()
                                onRequestFocus(false)
                                onInteract()
                                onDragDelta(dragAmount.x, dragAmount.y)
                            }
                        }
                    }
                    .padding(start = 12.dp, end = 8.dp, top = headerVerticalPad, bottom = headerVerticalPad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Read-only Note Title (Shifted to right to match button spacing & corner rounding)
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = TextStyle(
                        fontSize = (13.5f * actualHeaderScale).coerceIn(11f, 17f).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = if (note.isLocked) 0.65f else 0.95f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 8.dp)
                        .clickable { onInteract() }
                        .testTag("overlay_title_text")
                )

                // Compact Mode: Single 3-dots menu button
                if (actualIsCompactMode) {
                    Box(
                        modifier = Modifier
                            .size(actionButtonSize)
                            .clip(CircleShape)
                            .clickable {
                                onInteract()
                                showCompactMenu = !showCompactMenu
                            }
                            .testTag("overlay_compact_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Overlay Actions",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(actionIconSize)
                        )
                    }
                } else {
                    // Standard header with customizable button spacing & toggles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(actualButtonSpacing.dp)
                    ) {
                        // Lock Button
                        if (actualShowLock) {
                            Box(
                                modifier = Modifier
                                    .size(actionButtonSize)
                                    .clip(CircleShape)
                                    .clickable {
                                        onInteract()
                                        onToggleLock()
                                    }
                                    .testTag("overlay_lock_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (note.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (note.isLocked) "Unlock Note" else "Lock Note",
                                    tint = if (note.isLocked) Color(0xFFD32F2F) else textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }

                        // Opacity / Transparency Button
                        if (actualShowOpacity) {
                            Box(
                                modifier = Modifier
                                    .size(actionButtonSize)
                                    .clip(CircleShape)
                                    .clickable {
                                        onInteract()
                                        showOpacitySlider = !showOpacitySlider
                                    }
                                    .testTag("overlay_opacity_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = "Change Transparency",
                                    tint = if (showOpacitySlider) Color(0xFF1976D2) else textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }

                        // Minimize Button
                        if (actualShowMinimize) {
                            Box(
                                modifier = Modifier
                                    .size(actionButtonSize)
                                    .clip(CircleShape)
                                    .clickable {
                                        onInteract()
                                        onToggleMinimize()
                                    }
                                    .testTag("overlay_minimize_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minimize to Floating Bubble",
                                    tint = textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }

                        // Close Button
                        if (actualShowClose) {
                            Box(
                                modifier = Modifier
                                    .size(actionButtonSize)
                                    .clip(CircleShape)
                                    .clickable { onClose() }
                                    .testTag("overlay_close_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Floating Note",
                                    tint = textColor.copy(alpha = 0.85f),
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }
                        }
                    }
                }
            }

            // Compact Mode Menu Panel (Seamlessly adapted to header color, text color, and opacity)
            AnimatedVisibility(
                visible = showCompactMenu && actualIsCompactMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    color = headerColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock / Unlock Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showCompactMenu = false
                                    onToggleLock()
                                    onInteract()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("compact_action_lock")
                        ) {
                            Icon(
                                imageVector = if (note.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (note.isLocked) Color(0xFFD32F2F) else textColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (note.isLocked) "Unlock" else "Lock",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            )
                        }

                        // Transparency Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showCompactMenu = false
                                    showOpacitySlider = !showOpacitySlider
                                    onInteract()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("compact_action_opacity")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Opacity",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            )
                        }

                        // Minimize Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showCompactMenu = false
                                    onToggleMinimize()
                                    onInteract()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("compact_action_minimize")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Minimize",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            )
                        }

                        // Close Action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showCompactMenu = false
                                    onClose()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("compact_action_close")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Close",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            )
                        }
                    }
                }
            }

            // Transparency Popup Slider (Background adapts to opacity in real time)
            AnimatedVisibility(
                visible = showOpacitySlider,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    color = headerColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transparency",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "${(note.opacity * 100).roundToInt()}%",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                        }

                        Slider(
                            value = note.opacity,
                            onValueChange = {
                                onOpacityChange(it)
                                onInteract()
                            },
                            valueRange = 0.20f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = textColor,
                                activeTrackColor = textColor,
                                inactiveTrackColor = textColor.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("opacity_slider")
                        )

                        // Quick Opacity Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(0.35f to "35%", 0.60f to "60%", 0.85f to "85%", 1.0f to "100%").forEach { (valOp, label) ->
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if ((note.opacity - valOp).let { it in -0.05f..0.05f })
                                            textColor
                                        else
                                            textColor.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            onOpacityChange(valOp)
                                            onInteract()
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Note Content Editor Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 8.dp)
                    .verticalScroll(contentScrollState)
                    .then(
                        if (!note.isLocked) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onInteract()
                                onRequestFocus(true)
                                try {
                                    focusRequester.requestFocus()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else Modifier
                    )
            ) {
                if (note.isLocked) {
                    // Read-only text display when locked
                    Text(
                        text = note.content.ifBlank { "Locked note (empty content)" },
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = note.fontSize.sp,
                            fontWeight = fontWeight,
                            color = textColor.copy(alpha = if (note.content.isBlank()) 0.4f else 0.95f),
                            lineHeight = (note.fontSize * 1.35).sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInteract() }
                            .testTag("overlay_locked_content_view")
                    )
                } else {
                    // Editable Text field with focus requester & tap recovery
                    CompositionLocalProvider(LocalTextToolbar provides overlayTextToolbar) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                if (newValue.text != note.content) {
                                    onContentChange(newValue.text)
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = note.fontSize.sp,
                                fontWeight = fontWeight,
                                color = textColor,
                                lineHeight = (note.fontSize * 1.35).sp
                            ),
                            cursorBrush = if (isContentFocused) SolidColor(textColor) else SolidColor(Color.Transparent),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 80.dp)
                                ) {
                                    if (textFieldValue.text.isEmpty()) {
                                        Text(
                                            text = "Tap to write note here...",
                                            style = TextStyle(
                                                fontFamily = fontFamily,
                                                fontSize = note.fontSize.sp,
                                                fontWeight = fontWeight,
                                                color = textColor.copy(alpha = 0.45f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 80.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isContentFocused = focusState.isFocused
                                    onRequestFocus(focusState.isFocused)
                                    if (focusState.isFocused) {
                                        onInteract()
                                    } else {
                                        textMenuState = null
                                    }
                                }
                                .testTag("overlay_note_editor_input")
                        )
                    }
                }
            }
        }

        // Floating Scrollable Context Menu for Text Selection (Cut, Copy, Paste, Select All)
        AnimatedVisibility(
            visible = textMenuState != null && !note.isLocked,
            enter = fadeIn() + slideInVertically { -15 },
            exit = fadeOut() + slideOutVertically { -15 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (42 * actualHeaderScale).coerceIn(32f, 54f).dp)
                .zIndex(20f)
        ) {
            textMenuState?.let { menu ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("overlay_text_context_menu")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        menu.onCut?.let { onCut ->
                            TextButton(
                                onClick = {
                                    onCut()
                                    textMenuState = null
                                    onInteract()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("context_cut_button")
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cut", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        menu.onCopy?.let { onCopy ->
                            TextButton(
                                onClick = {
                                    onCopy()
                                    textMenuState = null
                                    onInteract()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("context_copy_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        menu.onPaste?.let { onPaste ->
                            TextButton(
                                onClick = {
                                    onPaste()
                                    textMenuState = null
                                    onInteract()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("context_paste_button")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        menu.onSelectAll?.let { onSelectAll ->
                            TextButton(
                                onClick = {
                                    onSelectAll()
                                    onInteract()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("context_select_all_button")
                            ) {
                                Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Resizing Handle (Bottom-Right corner)
        if (!note.isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            focusManager.clearFocus()
                            onRequestFocus(false)
                            onInteract()
                            onResizeDelta(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(end = 4.dp, bottom = 4.dp)
                    .testTag("overlay_resize_handle"),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    imageVector = Icons.Default.NorthWest,
                    contentDescription = "Resize window",
                    tint = textColor.copy(alpha = 0.45f),
                    modifier = Modifier.size(7.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingBubbleView(
    note: Note,
    onExpand: () -> Unit,
    onDragDelta: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onInteract: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val headerColor = Color(note.headerColor).copy(alpha = note.opacity)
    val textColor = Color(note.textColor)
    val borderColor = Color(note.borderColor)

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(headerColor)
            .border(2.dp, borderColor, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onInteract()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            }
            .clickable {
                onInteract()
                onExpand()
            }
            .testTag("floating_bubble_${note.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = note.title.take(3).ifBlank { "..." },
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                maxLines = 1
            )
        }
    }
}
