package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.DefaultPreset
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import com.example.service.OverlayNotesService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val prefsManager: PreferencesManager = PreferencesManager(application)

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        // Seed with welcoming sample sticky notes once on first app launch
        viewModelScope.launch {
            if (!prefsManager.hasSeededInitialNotes()) {
                prefsManager.setSeededInitialNotes(true)
                seedInitialNotes()
            }
        }
    }

    val searchQuery = MutableStateFlow("")
    val filterColor = MutableStateFlow<Long?>(null)
    val showPermissionDialog = MutableStateFlow(false)
    val isDynamicMonetEnabled = MutableStateFlow(prefsManager.isDynamicMonetEnabled())
    val themeMode = MutableStateFlow(prefsManager.getThemeMode())

    val notes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        searchQuery,
        filterColor
    ) { all, query, color ->
        all.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesColor = color == null || note.backgroundColor == color
            matchesQuery && matchesColor
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setDynamicMonet(enabled: Boolean) {
        isDynamicMonetEnabled.value = enabled
        prefsManager.setDynamicMonetEnabled(enabled)
    }

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefsManager.setThemeMode(mode)
    }

    fun getDefaultPreset(): DefaultPreset = prefsManager.getDefaultPreset()

    fun createNote(
        title: String = "",
        content: String = ""
    ) {
        val preset = prefsManager.getDefaultPreset()
        val newNote = Note(
            title = title,
            content = content,
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
        viewModelScope.launch {
            repository.insert(newNote)
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insert(note)
            } else {
                repository.update(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun deleteNotes(notesToDelete: List<Note>) {
        viewModelScope.launch {
            for (n in notesToDelete) {
                repository.delete(n)
            }
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            val duplicate = note.copy(
                id = 0,
                title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insert(duplicate)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(isPinned = !note.isPinned))
        }
    }

    fun floatNote(context: Context, note: Note) {
        if (Settings.canDrawOverlays(context)) {
            OverlayNotesService.startForNote(context, note.id)
        } else {
            showPermissionDialog.value = true
        }
    }

    suspend fun exportNotesJson(): String {
        val list = repository.allNotes.first()
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("title", item.title)
                put("content", item.content)
                put("backgroundColor", item.backgroundColor)
                put("headerColor", item.headerColor)
                put("textColor", item.textColor)
                put("fontFamily", item.fontFamily)
                put("fontSize", item.fontSize)
                put("isBold", item.isBold)
                put("cornerRadius", item.cornerRadius)
                put("borderWidth", item.borderWidth)
                put("borderColor", item.borderColor)
                put("elevation", item.elevation)
                put("opacity", item.opacity.toDouble())
                put("widthDp", item.widthDp)
                put("heightDp", item.heightDp)
                put("isPinned", item.isPinned)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    suspend fun importNotesFromJson(jsonString: String): Int {
        var count = 0
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val note = Note(
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    backgroundColor = obj.optLong("backgroundColor", 0xFFFFF9C4),
                    headerColor = obj.optLong("headerColor", 0xFFFFF176),
                    textColor = obj.optLong("textColor", 0xFF1C1B1F),
                    fontFamily = obj.optString("fontFamily", "sans-serif"),
                    fontSize = obj.optInt("fontSize", 15),
                    isBold = obj.optBoolean("isBold", false),
                    cornerRadius = obj.optInt("cornerRadius", 16),
                    borderWidth = obj.optInt("borderWidth", 1),
                    borderColor = obj.optLong("borderColor", 0x26000000),
                    elevation = obj.optInt("elevation", 6),
                    opacity = obj.optDouble("opacity", 0.95).toFloat(),
                    widthDp = obj.optInt("widthDp", 280),
                    heightDp = obj.optInt("heightDp", 300),
                    isPinned = obj.optBoolean("isPinned", false)
                )
                repository.insert(note)
                count++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    private suspend fun seedInitialNotes() {
        val welcomeNote = Note(
            title = "Welcome to Overlay Notes! 📌",
            content = "This sticky note can float on top of other apps!\n\n" +
                    "Try these interactive header controls:\n" +
                    "• ✥ Drag Header to reposition\n" +
                    "• 👁 Opacity icon for transparency\n" +
                    "• ➖ Minimize to a floating bubble\n" +
                    "• 🔒 Lock to read without accidental edits\n" +
                    "• ↘ Drag bottom-right corner to resize",
            backgroundColor = 0xFFFFF9C4, // Pastel Yellow
            headerColor = 0xFFFFF176,
            textColor = 0xFF1C1B1F,
            isPinned = true
        )

        val taskNote = Note(
            title = "Today's Focus 🎯",
            content = "1. Review project design specs\n" +
                    "2. Test floating overlay window\n" +
                    "3. Adjust sticky note colors & fonts\n" +
                    "4. Try minimize into edge bubble!",
            backgroundColor = 0xFFE8F5E9, // Mint
            headerColor = 0xFFC8E6C9,
            textColor = 0xFF1B3820,
            isPinned = true
        )

        val readingNote = Note(
            title = "Article Quotes & Cheat Sheet",
            content = "\"Simplicity is prerequisite for reliability.\"\n" +
                    "— Edsger W. Dijkstra\n\n" +
                    "Lock this note and keep it floating while reading articles or documentation.",
            backgroundColor = 0xFFE3F2FD, // Sky Blue
            headerColor = 0xFFBBDEFB,
            textColor = 0xFF0D2D44
        )

        repository.insert(welcomeNote)
        repository.insert(taskNote)
        repository.insert(readingNote)
    }
}
