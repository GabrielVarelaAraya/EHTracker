package com.example.ehtracker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ehtracker.ui.analytics.AnalyticsScreen
import com.example.ehtracker.ui.analytics.AnalyticsViewModel
import com.example.ehtracker.ui.calendar.CalendarViewModel
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
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.Factory(repository)
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(repository)
    )

    val dashboardState by dashboardViewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        dashboardViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        bottomBar = {
            if (!showSettings) {
                BottomNavBar(
                    currentPage = pagerState.currentPage,
                    onNavigate = { page ->
                        scope.launch { pagerState.animateScrollToPage(page) }
                    },
                    onAddClick = { dashboardViewModel.showAdd() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(state = pagerState) { page ->
                val pageAlpha by animateFloatAsState(
                    targetValue = if (page == pagerState.currentPage) 1f else 0.4f,
                    animationSpec = tween(350),
                    label = "pageFade"
                )
                val pageScale by animateFloatAsState(
                    targetValue = if (page == pagerState.currentPage) 1f else 0.9f,
                    animationSpec = tween(350),
                    label = "pageScale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = pageAlpha
                            scaleX = pageScale
                            scaleY = pageScale
                        }
                ) {
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = dashboardViewModel,
                            calendarViewModel = calendarViewModel,
                            onNavigateToSettings = { showSettings = true }
                        )
                        1 -> AnalyticsScreen(viewModel = analyticsViewModel)
                        2 -> LogsScreen(viewModel = logsViewModel)
                    }
                }
            }
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                BackHandler { showSettings = false }
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { showSettings = false }
                )
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
            onAddHabit = { name, icon, targetDays, isNumeric, unit ->
                dashboardViewModel.addHabit(name, icon, targetDays, isNumeric, unit)
            },
            currencySymbol = dashboardState.currency.symbol
        )
    }
}
