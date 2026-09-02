package com.example.ehtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.glance.appwidget.updateAll
import com.example.ehtracker.ui.theme.EHTrackerTheme
import com.example.ehtracker.ui.widget.BalanceWidget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val openQuickAddState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openQuickAddState.value = intent?.getBooleanExtra(EXTRA_OPEN_QUICK_ADD, false) == true
        setContent {
            val app = LocalContext.current.applicationContext as EHTrackerApplication
            val themeMode by app.repository.themeMode().collectAsState(initial = app.cachedThemeMode)
            EHTrackerTheme(themeMode = themeMode) {
                EHTrackerApp(openQuickAdd = openQuickAddState.value)
            }
        }

        GlobalScope.launch {
            runCatching { BalanceWidget().updateAll(applicationContext) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openQuickAddState.value = intent?.getBooleanExtra(EXTRA_OPEN_QUICK_ADD, false) == true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            com.example.ehtracker.ui.settings.NotificationPermissionBridge.deliverResult(
                grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            )
        }
    }

    companion object {
        const val EXTRA_OPEN_QUICK_ADD = "open_quick_add"
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    }
}
