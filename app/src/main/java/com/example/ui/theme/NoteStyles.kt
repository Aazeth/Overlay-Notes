package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.ColorUtils
import com.example.data.model.CustomFont
import com.example.data.model.Note
import java.io.File

data class NoteColorPreset(
    val name: String,
    val backgroundColor: Long,
    val headerColor: Long,
    val textColor: Long,
    val borderColor: Long,
    val darkBackgroundColor: Long = backgroundColor,
    val darkHeaderColor: Long = headerColor,
    val darkTextColor: Long = textColor,
    val darkBorderColor: Long = borderColor,
    val isMonet: Boolean = false
)

data class ResolvedNoteColors(
    val backgroundColor: Color,
    val headerColor: Color,
    val textColor: Color,
    val borderColor: Color
)

object NoteStyles {
    val StandardColorPresets = listOf(
        // 1. Sky Breeze (Blue - Default)
        NoteColorPreset(
            name = "Sky Breeze",
            backgroundColor = 0xFFE3F2FD,
            headerColor = 0xFFBBDEFB,
            textColor = 0xFF0D2D44,
            borderColor = 0x3364B5F6,
            darkBackgroundColor = 0xFF1C2C3D,
            darkHeaderColor = 0xFF28425C,
            darkTextColor = 0xFFE3F2FD,
            darkBorderColor = 0x4464B5F6
        ),
        // 2. Pastel Lemon (Yellow)
        NoteColorPreset(
            name = "Pastel Lemon",
            backgroundColor = 0xFFFFF9C4,
            headerColor = 0xFFFFF176,
            textColor = 0xFF1C1B1F,
            borderColor = 0x33FBC02D,
            darkBackgroundColor = 0xFF3D351A,
            darkHeaderColor = 0xFF544922,
            darkTextColor = 0xFFFFF9C4,
            darkBorderColor = 0x44FBC02D
        ),
        // 3. Mint Fresh (Green)
        NoteColorPreset(
            name = "Mint Fresh",
            backgroundColor = 0xFFE8F5E9,
            headerColor = 0xFFC8E6C9,
            textColor = 0xFF1B3820,
            borderColor = 0x3381C784,
            darkBackgroundColor = 0xFF1B3320,
            darkHeaderColor = 0xFF274A2E,
            darkTextColor = 0xFFE8F5E9,
            darkBorderColor = 0x4481C784
        ),
        // 4. Lavender Mist (Purple)
        NoteColorPreset(
            name = "Lavender Mist",
            backgroundColor = 0xFFF3E5F5,
            headerColor = 0xFFE1BEE7,
            textColor = 0xFF35153E,
            borderColor = 0x33BA68C8,
            darkBackgroundColor = 0xFF341F3A,
            darkHeaderColor = 0xFF4A2B52,
            darkTextColor = 0xFFF3E5F5,
            darkBorderColor = 0x44BA68C8
        ),
        // 5. Warm Peach (Orange)
        NoteColorPreset(
            name = "Warm Peach",
            backgroundColor = 0xFFFFE0B2,
            headerColor = 0xFFFFCC80,
            textColor = 0xFF3E2713,
            borderColor = 0x33FFB74D,
            darkBackgroundColor = 0xFF3D2614,
            darkHeaderColor = 0xFF56361C,
            darkTextColor = 0xFFFFE0B2,
            darkBorderColor = 0x44FFB74D
        ),
        // 6. Rose Petal (Pink/Red)
        NoteColorPreset(
            name = "Rose Petal",
            backgroundColor = 0xFFFFEBEE,
            headerColor = 0xFFFFCDD2,
            textColor = 0xFF42171B,
            borderColor = 0x33E57373,
            darkBackgroundColor = 0xFF3B1B20,
            darkHeaderColor = 0xFF54262E,
            darkTextColor = 0xFFFFEBEE,
            darkBorderColor = 0x44E57373
        ),
        // 7. Minimalist White (Charcoal in Dark Mode)
        NoteColorPreset(
            name = "Minimalist White",
            backgroundColor = 0xFFFFFFFF,
            headerColor = 0xFFF1F3F4,
            textColor = 0xFF202124,
            borderColor = 0x22000000,
            darkBackgroundColor = 0xFF202124,
            darkHeaderColor = 0xFF2D2E30,
            darkTextColor = 0xFFE8EAED,
            darkBorderColor = 0x445F6368
        ),
        // 8. Dark Slate (Charcoal)
        NoteColorPreset(
            name = "Dark Slate",
            backgroundColor = 0xFFECEFF1,
            headerColor = 0xFFCFD8DC,
            textColor = 0xFF263238,
            borderColor = 0x3378909C,
            darkBackgroundColor = 0xFF263238,
            darkHeaderColor = 0xFF37474F,
            darkTextColor = 0xFFECEFF1,
            darkBorderColor = 0x4478909C
        ),
        // 9. Midnight OLED
        NoteColorPreset(
            name = "Midnight OLED",
            backgroundColor = 0xFFF5F5F5,
            headerColor = 0xFFE0E0E0,
            textColor = 0xFF212121,
            borderColor = 0x339E9E9E,
            darkBackgroundColor = 0xFF121212,
            darkHeaderColor = 0xFF1E1E1E,
            darkTextColor = 0xFFF5F5F5,
            darkBorderColor = 0x44444444
        ),
        // 10. Warm Parchment (Sand)
        NoteColorPreset(
            name = "Warm Parchment",
            backgroundColor = 0xFFFDF6E2,
            headerColor = 0xFFF5E8C7,
            textColor = 0xFF2C2416,
            borderColor = 0x44D7C49E,
            darkBackgroundColor = 0xFF2E281D,
            darkHeaderColor = 0xFF433B2B,
            darkTextColor = 0xFFFDF6E2,
            darkBorderColor = 0x44D7C49E
        )
    )

    val ColorPresets: List<NoteColorPreset> get() = StandardColorPresets

    fun resolveNoteColors(note: Note, isDark: Boolean): ResolvedNoteColors {
        return resolveColors(
            bgColor = note.backgroundColor,
            headerColor = note.headerColor,
            textColor = note.textColor,
            borderColor = note.borderColor,
            isDark = isDark
        )
    }

    fun resolveColors(
        bgColor: Long,
        headerColor: Long,
        textColor: Long,
        borderColor: Long,
        isDark: Boolean
    ): ResolvedNoteColors {
        if (!isDark) {
            return ResolvedNoteColors(
                backgroundColor = Color(bgColor),
                headerColor = Color(headerColor),
                textColor = Color(textColor),
                borderColor = Color(borderColor)
            )
        }

        // Dark mode: Check if matching standard preset (by light or dark bgColor)
        val matched = StandardColorPresets.find {
            it.backgroundColor == bgColor || it.darkBackgroundColor == bgColor
        }

        if (matched != null) {
            return ResolvedNoteColors(
                backgroundColor = Color(matched.darkBackgroundColor),
                headerColor = Color(matched.darkHeaderColor),
                textColor = Color(matched.darkTextColor),
                borderColor = Color(matched.darkBorderColor)
            )
        }

        // Fallback for custom or Monet colors in Dark Mode
        val bgArgb = Color(bgColor).toArgb()
        val lum = ColorUtils.calculateLuminance(bgArgb)
        return if (lum > 0.4) {
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(bgArgb, hsl)
            hsl[1] = (hsl[1] * 0.45f).coerceIn(0.1f, 0.45f)
            hsl[2] = 0.16f
            val darkBg = ColorUtils.HSLToColor(hsl)
            hsl[2] = 0.24f
            val darkHeader = ColorUtils.HSLToColor(hsl)
            ResolvedNoteColors(
                backgroundColor = Color(darkBg),
                headerColor = Color(darkHeader),
                textColor = Color(0xFFF1F3F4),
                borderColor = Color(0x33FFFFFF)
            )
        } else {
            ResolvedNoteColors(
                backgroundColor = Color(bgColor),
                headerColor = Color(headerColor),
                textColor = Color(textColor),
                borderColor = Color(borderColor)
            )
        }
    }

    fun getMonetPresets(colorScheme: ColorScheme): List<NoteColorPreset> {
        val primaryContainerArgb = colorScheme.primaryContainer.toArgb().toLong() and 0xFFFFFFFFL
        val primaryArgb = colorScheme.primary.toArgb().toLong() and 0xFFFFFFFFL
        val onPrimaryContainerArgb = colorScheme.onPrimaryContainer.toArgb().toLong() and 0xFFFFFFFFL

        val secondaryContainerArgb = colorScheme.secondaryContainer.toArgb().toLong() and 0xFFFFFFFFL
        val secondaryArgb = colorScheme.secondary.toArgb().toLong() and 0xFFFFFFFFL
        val onSecondaryContainerArgb = colorScheme.onSecondaryContainer.toArgb().toLong() and 0xFFFFFFFFL

        val tertiaryContainerArgb = colorScheme.tertiaryContainer.toArgb().toLong() and 0xFFFFFFFFL
        val tertiaryArgb = colorScheme.tertiary.toArgb().toLong() and 0xFFFFFFFFL
        val onTertiaryContainerArgb = colorScheme.onTertiaryContainer.toArgb().toLong() and 0xFFFFFFFFL

        return listOf(
            NoteColorPreset(
                name = "Monet Primary",
                backgroundColor = primaryContainerArgb,
                headerColor = primaryArgb,
                textColor = onPrimaryContainerArgb,
                borderColor = 0x44FFFFFF,
                isMonet = true
            ),
            NoteColorPreset(
                name = "Monet Secondary",
                backgroundColor = secondaryContainerArgb,
                headerColor = secondaryArgb,
                textColor = onSecondaryContainerArgb,
                borderColor = 0x44FFFFFF,
                isMonet = true
            ),
            NoteColorPreset(
                name = "Monet Tertiary",
                backgroundColor = tertiaryContainerArgb,
                headerColor = tertiaryArgb,
                textColor = onTertiaryContainerArgb,
                borderColor = 0x44FFFFFF,
                isMonet = true
            )
        )
    }

    val AvailableFontFamilies = listOf(
        "sans-serif" to "Sans-Serif",
        "serif" to "Serif",
        "monospace" to "Monospace",
        "cursive" to "Handwriting / Cursive"
    )

    private val customFontCache = mutableMapOf<String, FontFamily>()

    fun resolveFontFamily(name: String): FontFamily {
        if (name.startsWith("custom:")) {
            val path = name.removePrefix("custom:")
            return customFontCache.getOrPut(path) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        FontFamily(Font(file))
                    } else {
                        FontFamily.SansSerif
                    }
                } catch (e: Exception) {
                    FontFamily.SansSerif
                }
            }
        }
        return when (name.lowercase()) {
            "serif" -> FontFamily.Serif
            "monospace" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            else -> FontFamily.SansSerif
        }
    }

    fun resolveFontWeight(isBold: Boolean): FontWeight {
        return if (isBold) FontWeight.Bold else FontWeight.Normal
    }

    fun colorFromLong(colorValue: Long): Color = Color(colorValue)
}
