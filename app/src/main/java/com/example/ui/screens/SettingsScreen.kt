package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.CustomFont
import com.example.data.model.DefaultPreset
import com.example.data.model.Note
import com.example.ui.components.ColorPickerSection
import com.example.ui.components.FontTypographySection
import com.example.ui.components.ShapeStyleSection
import com.example.ui.overlay.OverlayNoteWindow
import com.example.ui.viewmodel.NotesViewModel
import com.example.util.AdaptStatusBarColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val prefsManager = remember { PreferencesManager(context) }

    var currentPreset by remember { mutableStateOf(prefsManager.getDefaultPreset()) }
    val customFonts = remember { mutableStateListOf<CustomFont>().apply { addAll(prefsManager.getCustomFonts()) } }

    var isPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Overlay Header Customization States (Auto-saved immediately)
    var overlayHeaderScale by remember { mutableFloatStateOf(prefsManager.getOverlayHeaderScale()) }
    var overlayButtonSpacing by remember { mutableIntStateOf(prefsManager.getOverlayButtonSpacing()) }
    var isOverlayCompactMode by remember { mutableStateOf(prefsManager.isOverlayCompactMode()) }
    var isOverlayShowLock by remember { mutableStateOf(prefsManager.isOverlayShowLock()) }
    var isOverlayShowOpacity by remember { mutableStateOf(prefsManager.isOverlayShowOpacity()) }
    var isOverlayShowMinimize by remember { mutableStateOf(prefsManager.isOverlayShowMinimize()) }
    var isOverlayShowClose by remember { mutableStateOf(prefsManager.isOverlayShowClose()) }

    // Instant persistence helper: saves default preset instantly upon every tweak
    fun updatePresetInstant(updated: DefaultPreset) {
        currentPreset = updated
        prefsManager.saveDefaultPreset(updated)
    }

    // Real File Export Launcher (JSON file creation via SAF)
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = viewModel.exportNotesJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    snackbarHostState.showSnackbar("Notes backup exported successfully to JSON file!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Export failed: ${e.message}")
                }
            }
        }
    }

    // Real File Import Launcher (JSON file picker via SAF)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonString = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.bufferedReader(Charsets.UTF_8).readText()
                        } ?: ""
                    }
                    val count = viewModel.importNotesFromJson(jsonString)
                    snackbarHostState.showSnackbar("Successfully imported $count notes from file!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Import failed: ${e.message}")
                }
            }
        }
    }

    // Font file picker
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val fontName = getFileNameFromUri(context, it).ifBlank { "CustomFont_${System.currentTimeMillis()}" }
                    val cleanName = fontName.substringBeforeLast(".")
                    val destFile = File(context.filesDir, "font_${UUID.randomUUID().toString().take(8)}.ttf")

                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    val custom = CustomFont(
                        id = UUID.randomUUID().toString(),
                        displayName = cleanName,
                        filePath = destFile.absolutePath
                    )
                    prefsManager.saveCustomFont(custom)
                    customFonts.clear()
                    customFonts.addAll(prefsManager.getCustomFonts())
                    snackbarHostState.showSnackbar("Font '$cleanName' added successfully!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Failed to import font: ${e.message}")
                }
            }
        }
    }

    AdaptStatusBarColor(headerColor = MaterialTheme.colorScheme.surface)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Defaults",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Notes"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Permission Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPermissionGranted)
                        Color(0xFFE8F5E9)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPermissionGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPermissionGranted) "Overlay Permission Active" else "Overlay Permission Required",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isPermissionGranted) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (isPermissionGranted)
                                    "Floating sticky notes are ready to display over other apps."
                                else
                                    "Allow 'Display over other apps' to use floating notes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPermissionGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (!isPermissionGranted) {
                        Button(
                            onClick = {
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // File-Based Backup & Restore Section (Lag-Free)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup & Restore (JSON File)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export your entire collection of sticky notes directly to a JSON file or restore from a saved file without lag.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportFileLauncher.launch("overlay_notes_backup.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_notes_button")
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export File")
                        }

                        Button(
                            onClick = {
                                importFileLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_notes_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import File")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Theme Mode Section (Follow System, Light, Dark)
            val currentThemeMode by viewModel.themeMode.collectAsState()
            val dynamicMonet by viewModel.isDynamicMonetEnabled.collectAsState()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Theme Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your preferred theme mode for the app and overlay windows.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme Options Choice Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themeOptions = listOf(
                            "system" to "System",
                            "light" to "Light",
                            "dark" to "Dark"
                        )

                        themeOptions.forEach { (modeKey, label) ->
                            val isSelected = currentThemeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.setThemeMode(modeKey)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("theme_mode_${modeKey}_chip"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Monet Color Switch (Android 12+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Dynamic Colors (Monet)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Use system wallpaper color accents when supported.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dynamicMonet,
                            onCheckedChange = { viewModel.setDynamicMonet(it) },
                            modifier = Modifier.testTag("dynamic_monet_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Overlay Header Customization & Compact Mode Section (Auto-saved)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Overlay Header & Buttons",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compact Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Compact Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Condenses all header buttons into a single 3-dots menu containing close, minimize, transparency, and lock.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOverlayCompactMode,
                            onCheckedChange = {
                                isOverlayCompactMode = it
                                prefsManager.setOverlayCompactMode(it)
                            },
                            modifier = Modifier.testTag("compact_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Header Size Slider (Default 100%)
                    val scalePercent = (overlayHeaderScale * 100).roundToInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Header Size Scale",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (scalePercent == 60) "60% (Default)" else "$scalePercent%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = overlayHeaderScale,
                        onValueChange = {
                            overlayHeaderScale = it
                            prefsManager.setOverlayHeaderScale(it)
                        },
                        valueRange = 0.60f..1.40f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button Spacing Slider (Safe default 0dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Button Spacing",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${overlayButtonSpacing}dp" + if (overlayButtonSpacing == 0) " (Default)" else "",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = overlayButtonSpacing.toFloat(),
                        onValueChange = {
                            overlayButtonSpacing = it.roundToInt()
                            prefsManager.setOverlayButtonSpacing(it.roundToInt())
                        },
                        valueRange = 0f..8f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Button Visibility Toggles (Visible when Compact Mode is OFF)
                    if (!isOverlayCompactMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Visible Header Buttons:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Lock Button Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Lock Button", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isOverlayShowLock,
                                onCheckedChange = {
                                    isOverlayShowLock = it
                                    prefsManager.setOverlayShowLock(it)
                                }
                            )
                        }

                        // Opacity Button Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Transparency Button", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isOverlayShowOpacity,
                                onCheckedChange = {
                                    isOverlayShowOpacity = it
                                    prefsManager.setOverlayShowOpacity(it)
                                }
                            )
                        }

                        // Minimize Button Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Minimize Button", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isOverlayShowMinimize,
                                onCheckedChange = {
                                    isOverlayShowMinimize = it
                                    prefsManager.setOverlayShowMinimize(it)
                                }
                            )
                        }

                        // Close Button Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Close Button", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isOverlayShowClose,
                                onCheckedChange = {
                                    isOverlayShowClose = it
                                    prefsManager.setOverlayShowClose(it)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Default Preset Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Default New Note Style",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Changes are saved automatically in real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Non-interactive Live preview of Default Preset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewNote = Note(
                    id = -1L,
                    title = "Default Sticky Note",
                    content = "This is how your notes will look by default on screen.",
                    backgroundColor = currentPreset.backgroundColor,
                    headerColor = currentPreset.headerColor,
                    textColor = currentPreset.textColor,
                    fontFamily = currentPreset.fontFamily,
                    fontSize = currentPreset.fontSize,
                    isBold = currentPreset.isBold,
                    cornerRadius = currentPreset.cornerRadius,
                    borderWidth = currentPreset.borderWidth,
                    borderColor = currentPreset.borderColor,
                    elevation = currentPreset.elevation,
                    opacity = currentPreset.opacity
                )

                // Non-interactive card display
                Box(contentAlignment = Alignment.Center) {
                    OverlayNoteWindow(
                        note = previewNote,
                        headerScale = overlayHeaderScale,
                        buttonSpacing = overlayButtonSpacing,
                        isCompactMode = isOverlayCompactMode,
                        showLock = isOverlayShowLock,
                        showOpacity = isOverlayShowOpacity,
                        showMinimize = isOverlayShowMinimize,
                        showClose = isOverlayShowClose,
                        modifier = Modifier
                            .width(280.dp)
                            .height(180.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(enabled = false) {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Color Customization for Default (Auto-saved)
            ColorPickerSection(
                selectedBgColor = currentPreset.backgroundColor,
                onColorPresetSelected = { preset ->
                    updatePresetInstant(
                        currentPreset.copy(
                            backgroundColor = preset.backgroundColor,
                            headerColor = preset.headerColor,
                            textColor = preset.textColor,
                            borderColor = preset.borderColor
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Font & Sizing for Default (Auto-saved)
            FontTypographySection(
                selectedFontFamily = currentPreset.fontFamily,
                fontSize = currentPreset.fontSize,
                isBold = currentPreset.isBold,
                onFontFamilyChange = { updatePresetInstant(currentPreset.copy(fontFamily = it)) },
                onFontSizeChange = { updatePresetInstant(currentPreset.copy(fontSize = it)) },
                onBoldChange = { updatePresetInstant(currentPreset.copy(isBold = it)) },
                customFonts = customFonts,
                onImportFont = { fontPickerLauncher.launch(arrayOf("*/*")) },
                onDeleteCustomFont = { id ->
                    prefsManager.removeCustomFont(id)
                    customFonts.clear()
                    customFonts.addAll(prefsManager.getCustomFonts())
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Shape & Borders for Default (Auto-saved)
            ShapeStyleSection(
                cornerRadius = currentPreset.cornerRadius,
                borderWidth = currentPreset.borderWidth,
                opacity = currentPreset.opacity,
                onCornerRadiusChange = { updatePresetInstant(currentPreset.copy(cornerRadius = it)) },
                onBorderWidthChange = { updatePresetInstant(currentPreset.copy(borderWidth = it)) },
                onOpacityChange = { updatePresetInstant(currentPreset.copy(opacity = it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Defaults Button (Auto-saves on reset; manual save button removed)
            Button(
                onClick = {
                    val defaultFresh = DefaultPreset()
                    updatePresetInstant(defaultFresh)
                    overlayHeaderScale = 0.60f
                    prefsManager.setOverlayHeaderScale(0.60f)
                    overlayButtonSpacing = 0
                    prefsManager.setOverlayButtonSpacing(0)
                    isOverlayCompactMode = false
                    prefsManager.setOverlayCompactMode(false)
                    isOverlayShowLock = true
                    prefsManager.setOverlayShowLock(true)
                    isOverlayShowOpacity = true
                    prefsManager.setOverlayShowOpacity(true)
                    isOverlayShowMinimize = true
                    prefsManager.setOverlayShowMinimize(true)
                    isOverlayShowClose = true
                    prefsManager.setOverlayShowClose(true)
                    viewModel.setThemeMode("system")
                    scope.launch {
                        snackbarHostState.showSnackbar("All settings reset to defaults.")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_preset_defaults_button")
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Defaults", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // App Info Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About Overlay Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.1\n" +
                                "Lightweight floating sticky notes over other applications.\n" +
                                "Package: com.aazeth.overlaynotes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex) ?: ""
        }
    }
    return name
}
