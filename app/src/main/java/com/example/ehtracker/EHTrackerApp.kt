package com.example.ehtracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ehtracker.ui.analytics.AnalyticsScreen
import com.example.ehtracker.ui.analytics.AnalyticsViewModel
import com.example.ehtracker.ui.components.QuickAddSheet
import com.example.ehtracker.ui.dashboard.DashboardScreen
import com.example.ehtracker.ui.dashboard.DashboardViewModel
import com.example.ehtracker.ui.logs.LogsScreen
import com.example.ehtracker.ui.logs.LogsViewModel
import com.example.ehtracker.ui.navigation.BottomNavBar
import com.example.ehtracker.ui.navigation.Screen

@Composable
fun EHTrackerApp() {
    val context = LocalContext.current
    val app = context.applicationContext as EHTrackerApplication
    val repository = app.repository

    val navController = rememberNavController()

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(repository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.Factory(repository)
    )
    val logsViewModel: LogsViewModel = viewModel(
        factory = LogsViewModel.Factory(repository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showAddSheet by dashboardViewModel.showAddSheet.collectAsState()
    val currency by dashboardViewModel.currency.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = analyticsViewModel)
            }
            composable(Screen.Logs.route) {
                LogsScreen(viewModel = logsViewModel)
            }
        }

        if (showAddSheet) {
            QuickAddSheet(
                onDismiss = { dashboardViewModel.dismissAdd() },
                onAddExpense = { amount, category, note, date ->
                    dashboardViewModel.addExpense(amount, category, note, date)
                },
                onAddIncome = { amount, note ->
                    dashboardViewModel.addIncome(amount, note)
                },
                onAddHabit = { name, icon ->
                    dashboardViewModel.addHabit(name, icon)
                },
                currencySymbol = currency.symbol
            )
        }
    }
}
