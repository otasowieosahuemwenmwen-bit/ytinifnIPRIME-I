package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimeViolet

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
            val isPrivateMode by viewModel.isPrivateMode.collectAsStateWithLifecycle()

            val accentColor = try {
                Color(android.graphics.Color.parseColor(activeCategory.colorHex))
            } catch (e: Exception) {
                PrimeViolet
            }

            // If user presses back while in private mode, gracefully exit private mode
            BackHandler(enabled = isPrivateMode) {
                viewModel.togglePrivateMode(false)
            }

            MyApplicationTheme(accentColor = accentColor) {
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}
