package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NoteColorPreset
import com.example.ui.theme.NoteStyles

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerSection(
    selectedBgColor: Long,
    onColorPresetSelected: (NoteColorPreset) -> Unit,
    onCustomBgColorSelected: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Sticky Note Color",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        val isDark = androidx.compose.foundation.isSystemInDarkTheme()

        // Curated Presets Section (No 'More Colors')
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NoteStyles.StandardColorPresets.forEach { preset ->
                val isSelected = selectedBgColor == preset.backgroundColor || (isDark && selectedBgColor == preset.darkBackgroundColor)
                val chipBg = if (isDark) Color(preset.darkBackgroundColor) else Color(preset.backgroundColor)
                val chipHeader = if (isDark) Color(preset.darkHeaderColor) else Color(preset.headerColor)
                val checkTint = if (isDark) Color(preset.darkTextColor) else Color(preset.textColor)

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(chipBg)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(preset.borderColor),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onColorPresetSelected(preset) }
                        .testTag("color_preset_${preset.name.replace(" ", "_").lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(chipHeader)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = checkTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
