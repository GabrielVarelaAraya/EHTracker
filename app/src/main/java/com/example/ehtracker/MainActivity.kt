package com.example.ehtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.ehtracker.ui.theme.EHTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = LocalContext.current.applicationContext as EHTrackerApplication
            val themeMode by app.repository.themeMode().collectAsState(initial = app.cachedThemeMode)
            EHTrackerTheme(themeMode = themeMode) {
                EHTrackerApp()
            }
        }
    }
}
