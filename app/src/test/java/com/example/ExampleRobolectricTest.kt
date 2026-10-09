package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import com.example.data.model.CustomFont
import com.example.data.model.DefaultPreset
import com.example.data.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Overlay Notes", appName)
    }

    @Test
    fun `preferences manager handles default preset and dynamic monet`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        val defaultPreset = prefs.getDefaultPreset()
        assertNotNull(defaultPreset)
        assertEquals(0xFFE3F2FD, defaultPreset.backgroundColor)

        val updated = defaultPreset.copy(fontSize = 18, isBold = true)
        prefs.saveDefaultPreset(updated)
        val reloaded = prefs.getDefaultPreset()
        assertEquals(18, reloaded.fontSize)
        assertEquals(true, reloaded.isBold)

        assertTrue(prefs.isDynamicMonetEnabled())
        prefs.setDynamicMonetEnabled(false)
        assertEquals(false, prefs.isDynamicMonetEnabled())
    }

    @Test
    fun `preferences manager overlay header customization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        // Default 0.60f (60% scale)
        assertEquals(0.60f, prefs.getOverlayHeaderScale(), 0.01f)
        prefs.setOverlayHeaderScale(0.80f)
        assertEquals(0.80f, prefs.getOverlayHeaderScale(), 0.01f)

        // Button spacing default 0dp
        assertEquals(0, prefs.getOverlayButtonSpacing())
        prefs.setOverlayButtonSpacing(2)
        assertEquals(2, prefs.getOverlayButtonSpacing())

        // Compact mode default false
        assertEquals(false, prefs.isOverlayCompactMode())
        prefs.setOverlayCompactMode(true)
        assertEquals(true, prefs.isOverlayCompactMode())
    }

    @Test
    fun `preferences manager custom fonts storage`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        val custom = CustomFont(id = "1", displayName = "RobotoCustom", filePath = "/dummy/Roboto.ttf")
        prefs.saveCustomFont(custom)
        val fonts = prefs.getCustomFonts()
        assertTrue(fonts.any { it.id == "1" && it.displayName == "RobotoCustom" })

        prefs.removeCustomFont("1")
        val afterDelete = prefs.getCustomFonts()
        assertTrue(afterDelete.none { it.id == "1" })
    }

    @Test
    fun `create note entity defaults`() {
        val note = Note(title = "Test Note", content = "Test Content")
        assertEquals("Test Note", note.title)
        assertEquals("Test Content", note.content)
        assertEquals(false, note.isLocked)
        assertEquals(false, note.isMinimized)
    }
}
