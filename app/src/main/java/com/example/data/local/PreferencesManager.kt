package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CustomFont
import com.example.data.model.DefaultPreset
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("overlay_notes_prefs", Context.MODE_PRIVATE)

    fun getDefaultPreset(): DefaultPreset {
        return DefaultPreset(
            backgroundColor = prefs.getLong("default_bg_color", 0xFFE3F2FD),
            textColor = prefs.getLong("default_text_color", 0xFF0D2D44),
            headerColor = prefs.getLong("default_header_color", 0xFFBBDEFB),
            fontFamily = prefs.getString("default_font_family", "sans-serif") ?: "sans-serif",
            fontSize = prefs.getInt("default_font_size", 15),
            isBold = prefs.getBoolean("default_is_bold", false),
            cornerRadius = prefs.getInt("default_corner_radius", 12),
            borderWidth = prefs.getInt("default_border_width", 1),
            borderColor = prefs.getLong("default_border_color", 0x3364B5F6),
            elevation = prefs.getInt("default_elevation", 0),
            opacity = prefs.getFloat("default_opacity", 1.0f),
            widthDp = prefs.getInt("default_width_dp", 280),
            heightDp = prefs.getInt("default_height_dp", 300)
        )
    }

    fun hasSeededInitialNotes(): Boolean {
        return prefs.getBoolean("has_seeded_initial_notes", false)
    }

    fun setSeededInitialNotes(seeded: Boolean) {
        prefs.edit().putBoolean("has_seeded_initial_notes", seeded).commit()
    }

    fun saveDefaultPreset(preset: DefaultPreset) {
        prefs.edit()
            .putLong("default_bg_color", preset.backgroundColor)
            .putLong("default_text_color", preset.textColor)
            .putLong("default_header_color", preset.headerColor)
            .putString("default_font_family", preset.fontFamily)
            .putInt("default_font_size", preset.fontSize)
            .putBoolean("default_is_bold", preset.isBold)
            .putInt("default_corner_radius", preset.cornerRadius)
            .putInt("default_border_width", preset.borderWidth)
            .putLong("default_border_color", preset.borderColor)
            .putInt("default_elevation", preset.elevation)
            .putFloat("default_opacity", preset.opacity)
            .putInt("default_width_dp", preset.widthDp)
            .putInt("default_height_dp", preset.heightDp)
            .apply()
    }

    fun isDynamicMonetEnabled(): Boolean {
        return prefs.getBoolean("pref_dynamic_monet", true)
    }

    fun setDynamicMonetEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_dynamic_monet", enabled).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString("pref_theme_mode", "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("pref_theme_mode", mode).apply()
    }

    // Overlay Header Settings
    fun getOverlayHeaderScale(): Float {
        // Default 0.60f represents 60%
        return prefs.getFloat("pref_overlay_header_scale", 0.60f)
    }

    fun setOverlayHeaderScale(scale: Float) {
        prefs.edit().putFloat("pref_overlay_header_scale", scale).apply()
    }

    fun getOverlayButtonSpacing(): Int {
        // Default 0dp
        return prefs.getInt("pref_overlay_button_spacing", 0)
    }

    fun setOverlayButtonSpacing(spacingDp: Int) {
        prefs.edit().putInt("pref_overlay_button_spacing", spacingDp).apply()
    }

    fun isOverlayCompactMode(): Boolean {
        return prefs.getBoolean("pref_overlay_compact_mode", false)
    }

    fun setOverlayCompactMode(enabled: Boolean) {
        prefs.edit().putBoolean("pref_overlay_compact_mode", enabled).apply()
    }

    fun isOverlayShowLock(): Boolean {
        return prefs.getBoolean("pref_overlay_show_lock", true)
    }

    fun setOverlayShowLock(show: Boolean) {
        prefs.edit().putBoolean("pref_overlay_show_lock", show).apply()
    }

    fun isOverlayShowOpacity(): Boolean {
        return prefs.getBoolean("pref_overlay_show_opacity", true)
    }

    fun setOverlayShowOpacity(show: Boolean) {
        prefs.edit().putBoolean("pref_overlay_show_opacity", show).apply()
    }

    fun isOverlayShowMinimize(): Boolean {
        return prefs.getBoolean("pref_overlay_show_minimize", true)
    }

    fun setOverlayShowMinimize(show: Boolean) {
        prefs.edit().putBoolean("pref_overlay_show_minimize", show).apply()
    }

    fun isOverlayShowClose(): Boolean {
        return prefs.getBoolean("pref_overlay_show_close", true)
    }

    fun setOverlayShowClose(show: Boolean) {
        prefs.edit().putBoolean("pref_overlay_show_close", show).apply()
    }

    fun getCustomFonts(): List<CustomFont> {
        val json = prefs.getString("custom_fonts_list", "[]") ?: "[]"
        val list = mutableListOf<CustomFont>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomFont(
                        id = obj.getString("id"),
                        displayName = obj.getString("displayName"),
                        filePath = obj.getString("filePath")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCustomFont(font: CustomFont) {
        val list = getCustomFonts().toMutableList()
        list.removeAll { it.id == font.id || it.filePath == font.filePath }
        list.add(font)
        val array = JSONArray()
        for (f in list) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("displayName", f.displayName)
                put("filePath", f.filePath)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_fonts_list", array.toString()).apply()
    }

    fun removeCustomFont(fontId: String) {
        val list = getCustomFonts().filter { it.id != fontId }
        val array = JSONArray()
        for (f in list) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("displayName", f.displayName)
                put("filePath", f.filePath)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_fonts_list", array.toString()).apply()
    }
}
