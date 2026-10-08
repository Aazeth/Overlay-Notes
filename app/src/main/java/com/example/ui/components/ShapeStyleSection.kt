package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ShapeStyleSection(
    cornerRadius: Int,
    borderWidth: Int,
    opacity: Float,
    onCornerRadiusChange: (Int) -> Unit,
    onBorderWidthChange: (Int) -> Unit,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Shape & Visual Styling",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Corner Radius
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Corner Rounding",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "${cornerRadius}dp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = cornerRadius.toFloat(),
            onValueChange = { onCornerRadiusChange(it.roundToInt()) },
            valueRange = 0f..32f,
            steps = 31,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("corner_radius_slider")
        )

        // Quick corner presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0 to "Sharp (0)", 12 to "Subtle (12)", 18 to "Rounded (18)", 28 to "Pill (28)").forEach { (rad, label) ->
                FilterChip(
                    selected = cornerRadius == rad,
                    onClick = { onCornerRadiusChange(rad) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("corner_preset_$rad")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Border Width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Border Width",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = if (borderWidth == 0) "None" else "${borderWidth}dp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = borderWidth.toFloat(),
            onValueChange = { onBorderWidthChange(it.roundToInt()) },
            valueRange = 0f..4f,
            steps = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("border_width_slider")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Default Opacity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overlay Opacity",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "${(opacity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = 0.20f..1.0f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("style_opacity_slider")
        )
    }
}
