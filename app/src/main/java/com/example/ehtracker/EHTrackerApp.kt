package com.example.ehtracker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ehtracker.ui.analytics.AnalyticsScreen
import com.example.ehtracker.ui.analytics.AnalyticsViewModel
import com.example.ehtracker.ui.components.QuickAddSheet
import com.example.ehtracker.ui.dashboard.DashboardScreen
import com.example.ehtracker.ui.dashboard.DashboardViewModel
import com.example.ehtracker.ui.logs.LogsScreen
import com.example.ehtracker.ui.logs.LogsViewModel
import com.example.ehtracker.ui.navigation.BottomNavBar
import com.example.ehtracker.ui.settings.SettingsScreen
import com.example.ehtracker.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun EHTrackerApp() {
    val context = LocalContext.current
    val app = context.applicationContext as EHTrackerApplication
    val repository = app.repository

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(repository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.Factory(repository)
    )
    val logsViewModel: LogsViewModel = viewModel(
        factory = LogsViewModel.Factory(repository)
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(repository)
    )

    val dashboardState by dashboardViewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                currentPage = pagerState.currentPage,
                onNavigate = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showSettings) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { showSettings = false }
                )
            } else {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToSettings = { showSettings = true }
                        )
                        1 -> AnalyticsScreen(viewModel = analyticsViewModel)
                        2 -> LogsScreen(viewModel = logsViewModel)
                    }
                }
            }
        }
    }

    if (dashboardState.showAddSheet) {
        QuickAddSheet(
            onDismiss = { dashboardViewModel.dismissAdd() },
            onAddExpense = { amount, category, note, date ->
                dashboardViewModel.addExpense(amount, category, note, date)
            },
            onAddIncome = { amount, note, date ->
                dashboardViewModel.addIncome(amount, note, date)
            },
            onAddHabit = { name, icon, targetDays ->
                dashboardViewModel.addHabit(name, icon, targetDays)
            },
            currencySymbol = dashboardState.currency.symbol,
            lastExpenseAmount = dashboardState.lastExpenseAmount
        )
    }
}
