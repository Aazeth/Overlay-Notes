package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import com.example.ui.overlay.OverlayNoteWindow
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OverlayNotesService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private lateinit var repository: NoteRepository
    private val activeViews = mutableMapOf<Long, Pair<View, WindowManager.LayoutParams>>()
    private val lifecycleOwners = mutableMapOf<Long, OverlayLifecycleOwner>()

    companion object {
        const val CHANNEL_ID = "overlay_notes_channel"
        const val NOTIFICATION_ID = 8801

        const val ACTION_FLOAT_NOTE = "com.example.overlaynotes.ACTION_FLOAT_NOTE"
        const val ACTION_CLOSE_NOTE = "com.example.overlaynotes.ACTION_CLOSE_NOTE"
        const val ACTION_CLOSE_ALL = "com.example.overlaynotes.ACTION_CLOSE_ALL"
        const val EXTRA_NOTE_ID = "extra_note_id"

        fun startForNote(context: Context, noteId: Long) {
            val intent = Intent(context, OverlayNotesService::class.java).apply {
                action = ACTION_FLOAT_NOTE
                putExtra(EXTRA_NOTE_ID, noteId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun closeNote(context: Context, noteId: Long) {
            val intent = Intent(context, OverlayNotesService::class.java).apply {
                action = ACTION_CLOSE_NOTE
                putExtra(EXTRA_NOTE_ID, noteId)
            }
            context.startService(intent)
        }

        fun closeAll(context: Context) {
            val intent = Intent(context, OverlayNotesService::class.java).apply {
                action = ACTION_CLOSE_ALL
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val database = AppDatabase.getDatabase(this)
        repository = NoteRepository(database.noteDao())
        createNotificationChannel()
        startAsForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_FLOAT_NOTE -> {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    floatNote(noteId)
                }
            }
            ACTION_CLOSE_NOTE -> {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    removeOverlay(noteId)
                }
            }
            ACTION_CLOSE_ALL -> {
                removeAllOverlays()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun floatNote(noteId: Long) {
        if (activeViews.containsKey(noteId)) {
            // Already floating
            return
        }

        serviceScope.launch {
            val note = repository.getNoteByIdSync(noteId) ?: return@launch
            createFloatingNoteView(note)
        }
    }

    private fun createFloatingNoteView(initialNote: Note) {
        val density = resources.displayMetrics.density
        val minWidthPx = (125 * density).roundToInt()
        val minHeightPx = (100 * density).roundToInt()
        val maxWidthPx = (resources.displayMetrics.widthPixels * 0.95f).roundToInt()
        val maxHeightPx = (resources.displayMetrics.heightPixels * 0.85f).roundToInt()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val initialWidth = if (initialNote.isMinimized) {
            (56 * density).roundToInt()
        } else {
            (initialNote.widthDp * density).roundToInt().coerceIn(minWidthPx, maxWidthPx)
        }

        val initialHeight = if (initialNote.isMinimized) {
            (56 * density).roundToInt()
        } else {
            (initialNote.heightDp * density).roundToInt().coerceIn(minHeightPx, maxHeightPx)
        }

        val params = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialNote.posX.coerceAtLeast(0)
            y = initialNote.posY.coerceAtLeast(60)
        }

        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.onCreate()
        lifecycleOwners[initialNote.id] = lifecycleOwner

        lateinit var composeViewRef: ComposeView
        val composeView = ComposeView(this).apply {
            composeViewRef = this
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner))
            lifecycleOwner.attachToView(this)
            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_OUTSIDE) {
                    if ((params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0) {
                        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        try {
                            windowManager.updateViewLayout(this, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    this.clearFocus()
                }
                false
            }
            setContent {
                val prefsManager = androidx.compose.runtime.remember { PreferencesManager(this@OverlayNotesService) }
                val themeMode = androidx.compose.runtime.remember { prefsManager.getThemeMode() }
                val dynamicMonet = androidx.compose.runtime.remember { prefsManager.isDynamicMonetEnabled() }

                MyApplicationTheme(
                    themeMode = themeMode,
                    dynamicColor = dynamicMonet
                ) {
                    var currentNote by androidx.compose.runtime.remember { mutableStateOf(initialNote) }

                    OverlayNoteWindow(
                    note = currentNote,
                    isFloatingWindowManager = true,
                    onRequestFocus = { needFocus ->
                        val wasNotFocusable = (params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0
                        val shouldUpdate = if (needFocus) {
                            if (wasNotFocusable) {
                                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                                true
                            } else false
                        } else {
                            if (!wasNotFocusable) {
                                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                true
                            } else false
                        }
                        if (shouldUpdate) {
                            try {
                                windowManager.updateViewLayout(this, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onContentChange = { newContent ->
                        currentNote = currentNote.copy(content = newContent)
                        serviceScope.launch(Dispatchers.IO) {
                            repository.updateContent(currentNote.id, newContent)
                        }
                    },
                    onTitleChange = { newTitle ->
                        currentNote = currentNote.copy(title = newTitle)
                        serviceScope.launch(Dispatchers.IO) {
                            repository.update(currentNote)
                        }
                    },
                    onDragDelta = { dx, dy ->
                        params.x += dx.roundToInt()
                        params.y += dy.roundToInt()
                        try {
                            windowManager.updateViewLayout(this, params)
                            currentNote = currentNote.copy(posX = params.x, posY = params.y)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onResizeDelta = { dw, dh ->
                        if (!currentNote.isMinimized) {
                            params.width = (params.width + dw.roundToInt()).coerceIn(minWidthPx, maxWidthPx)
                            params.height = (params.height + dh.roundToInt()).coerceIn(minHeightPx, maxHeightPx)
                            try {
                                windowManager.updateViewLayout(this, params)
                                val newWidthDp = (params.width / density).roundToInt()
                                val newHeightDp = (params.height / density).roundToInt()
                                currentNote = currentNote.copy(widthDp = newWidthDp, heightDp = newHeightDp)
                                serviceScope.launch(Dispatchers.IO) {
                                    repository.updateGeometry(
                                        currentNote.id,
                                        newWidthDp,
                                        newHeightDp,
                                        params.x,
                                        params.y,
                                        currentNote.opacity
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onOpacityChange = { newOpacity ->
                        currentNote = currentNote.copy(opacity = newOpacity)
                        serviceScope.launch(Dispatchers.IO) {
                            repository.update(currentNote)
                        }
                    },
                    onToggleLock = {
                        val newLocked = !currentNote.isLocked
                        currentNote = currentNote.copy(isLocked = newLocked)
                        serviceScope.launch(Dispatchers.IO) {
                            repository.updateStatus(currentNote.id, newLocked, currentNote.isMinimized)
                        }
                    },
                    onToggleMinimize = {
                        val newMinimized = !currentNote.isMinimized
                        currentNote = currentNote.copy(isMinimized = newMinimized)
                        if (newMinimized) {
                            params.width = (56 * density).roundToInt()
                            params.height = (56 * density).roundToInt()
                        } else {
                            params.width = (currentNote.widthDp * density).roundToInt().coerceIn(minWidthPx, maxWidthPx)
                            params.height = (currentNote.heightDp * density).roundToInt().coerceIn(minHeightPx, maxHeightPx)
                        }
                        try {
                            windowManager.updateViewLayout(this, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        serviceScope.launch(Dispatchers.IO) {
                            repository.updateStatus(currentNote.id, currentNote.isLocked, newMinimized)
                        }
                    },
                    onClose = {
                        removeOverlay(currentNote.id)
                    }
                )
            }
        }
    }

        try {
            windowManager.addView(composeView, params)
            activeViews[initialNote.id] = Pair(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            lifecycleOwner.onDestroy()
            lifecycleOwners.remove(initialNote.id)
        }
    }

    private fun removeOverlay(noteId: Long) {
        activeViews[noteId]?.let { (view, _) ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeViews.remove(noteId)
        lifecycleOwners[noteId]?.onDestroy()
        lifecycleOwners.remove(noteId)

        if (activeViews.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun removeAllOverlays() {
        activeViews.forEach { (_, pair) ->
            try {
                windowManager.removeView(pair.first)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activeViews.clear()
        lifecycleOwners.forEach { (_, owner) -> owner.onDestroy() }
        lifecycleOwners.clear()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startAsForeground() {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val closeAllIntent = Intent(this, OverlayNotesService::class.java).apply {
            action = ACTION_CLOSE_ALL
        }
        val pendingCloseAll = PendingIntent.getService(
            this,
            1,
            closeAllIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentIntent(pendingOpenApp)
            .addAction(0, "Close Overlays", pendingCloseAll)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeAllOverlays()
        serviceScope.cancel()
        super.onDestroy()
    }
}
