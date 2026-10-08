package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.NotesListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NotesViewModel

enum class AppScreen {
    NOTES_LIST,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val notesViewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by notesViewModel.themeMode.collectAsState()
            val dynamicMonet by notesViewModel.isDynamicMonetEnabled.collectAsState()

            MyApplicationTheme(
                themeMode = themeMode,
                dynamicColor = dynamicMonet
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(AppScreen.NOTES_LIST) }

                    when (currentScreen) {
                        AppScreen.NOTES_LIST -> {
                            NotesListScreen(
                                viewModel = notesViewModel,
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                            )
                        }
                        AppScreen.SETTINGS -> {
                            BackHandler {
                                currentScreen = AppScreen.NOTES_LIST
                            }
                            SettingsScreen(
                                viewModel = notesViewModel,
                                onNavigateBack = { currentScreen = AppScreen.NOTES_LIST }
                            )
                        }
                    }
                }
            }
        }
    }
}
