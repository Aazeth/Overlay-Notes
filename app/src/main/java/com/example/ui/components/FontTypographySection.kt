package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomFont
import com.example.ui.theme.NoteStyles
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FontTypographySection(
    selectedFontFamily: String,
    fontSize: Int,
    isBold: Boolean,
    onFontFamilyChange: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onBoldChange: (Boolean) -> Unit,
    customFonts: List<CustomFont> = emptyList(),
    onImportFont: (() -> Unit)? = null,
    onDeleteCustomFont: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Typography & Sizing",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Standard System Fonts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NoteStyles.AvailableFontFamilies.forEach { (key, label) ->
                val isSelected = selectedFontFamily.equals(key, ignoreCase = true)
                val resolvedFamily = NoteStyles.resolveFontFamily(key)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onFontFamilyChange(key) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                        .testTag("font_family_chip_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Aa",
                            fontFamily = resolvedFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (key) {
                                "sans-serif" -> "Sans"
                                "serif" -> "Serif"
                                "monospace" -> "Mono"
                                "cursive" -> "Script"
                                else -> key
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Custom Imported Fonts Section
        if (customFonts.isNotEmpty() || onImportFont != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Local Fonts",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (onImportFont != null) {
                    OutlinedButton(
                        onClick = onImportFont,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("import_custom_font_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Font", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (customFonts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    customFonts.forEach { font ->
                        val customKey = "custom:${font.filePath}"
                        val isSelected = selectedFontFamily == customKey
                        val resolved = NoteStyles.resolveFontFamily(customKey)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clickable { onFontFamilyChange(customKey) }
                                .testTag("custom_font_chip_${font.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = if (onDeleteCustomFont != null) 4.dp else 10.dp, top = 6.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = font.displayName,
                                    fontFamily = resolved,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )

                                if (onDeleteCustomFont != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteCustomFont(font.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove font",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Font Size Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatSize,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Font Size",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "${fontSize}sp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = fontSize.toFloat(),
            onValueChange = { onFontSizeChange(it.roundToInt()) },
            valueRange = 12f..28f,
            steps = 15,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("font_size_slider")
        )

        // Bold toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Text Weight",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isBold,
                    onClick = { onBoldChange(false) },
                    label = { Text("Regular") },
                    colors = FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.testTag("font_weight_regular_chip")
                )

                FilterChip(
                    selected = isBold,
                    onClick = { onBoldChange(true) },
                    label = { Text("Bold") },
                    colors = FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.testTag("font_weight_bold_chip")
                )
            }
        }
    }
}
