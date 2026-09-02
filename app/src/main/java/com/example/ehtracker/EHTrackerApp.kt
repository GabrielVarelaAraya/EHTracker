package com.example.ehtracker

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ehtracker.ui.account.AccountSelectionScreen
import com.example.ehtracker.ui.analytics.AnalyticsScreen
import com.example.ehtracker.ui.analytics.AnalyticsViewModel
import com.example.ehtracker.ui.calendar.CalendarScreen
import com.example.ehtracker.ui.calendar.CalendarViewModel
import com.example.ehtracker.ui.components.QuickAddSheet
import com.example.ehtracker.ui.dashboard.DashboardScreen
import com.example.ehtracker.ui.dashboard.DashboardViewModel
import com.example.ehtracker.ui.logs.LogsScreen
import com.example.ehtracker.ui.logs.LogsViewModel
import com.example.ehtracker.ui.lock.LockScreen
import com.example.ehtracker.ui.navigation.BottomNavBar
import com.example.ehtracker.ui.settings.SettingsScreen
import com.example.ehtracker.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EHTrackerApp(openQuickAdd: Boolean = false) {
    val context = LocalContext.current
    val app = context.applicationContext as EHTrackerApplication
    val repository = app.repository
    val lockManager = app.lockManager

    val lockEnabled by lockManager.lockEnabled.collectAsState(initial = app.cachedAppLockEnabled)
    var isLocked by rememberSaveable { mutableStateOf(app.cachedAppLockEnabled) }
    var unlocking by remember { mutableStateOf(false) }

    LaunchedEffect(unlocking) {
        if (unlocking) {
            delay(900)
            unlocking = false
        }
    }

    val lifecycleOwner = ProcessLifecycleOwner.get()
    DisposableEffect(lifecycleOwner, lockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && lockEnabled) {
                isLocked = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(lockEnabled) {
        if (!lockEnabled && isLocked) {
            isLocked = false
        }
    }

    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val exitToAccounts: () -> Unit = {
        scope.launch { repository.setCurrentAccount(null) }
        selectedAccountId = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AccountSelectionScreen(
            repository = repository,
            lockManager = lockManager,
            onSelectAccount = { id ->
                scope.launch { repository.setCurrentAccount(id) }
                selectedAccountId = id
            }
        )

        AnimatedVisibility(
            visible = selectedAccountId != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            BackHandler(onBack = exitToAccounts)
            MainAppContent(
                repository = repository,
                lockManager = lockManager,
                onExitAccount = exitToAccounts,
                openQuickAdd = openQuickAdd
            )
        }

        AnimatedVisibility(
            visible = isLocked || unlocking,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* consume touches so they don't fall through */ }
            ) {
                Crossfade(
                    targetState = if (isLocked) 0 else 1,
                    animationSpec = tween(300),
                    label = "lockToLoading"
                ) { phase ->
                    when (phase) {
                        0 -> LockScreen(
                            lockManager = lockManager,
                            appName = context.getString(com.example.ehtracker.R.string.app_name),
                            onUnlocked = {
                                isLocked = false
                                unlocking = true
                            }
                        )
                        else -> UnlockLoadingScreen(appName = context.getString(com.example.ehtracker.R.string.app_name))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    repository: com.example.ehtracker.data.repository.TrackerRepository,
    lockManager: com.example.ehtracker.lock.AppLockManager,
    onExitAccount: () -> Unit,
    openQuickAdd: Boolean = false
) {
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

    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val analyticsBudgetStatus by analyticsViewModel.budgetStatus.collectAsState()
    val today = java.time.LocalDate.now()
    val showInsightsBadge = analyticsBudgetStatus.values.any { (actual, limit) -> actual > limit }
    val showHistoryBadge = dashboardState.subscriptions.any { it.isActive && it.billingDay == today.dayOfMonth }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        dashboardViewModel.snackbarEvent.collect { message ->
            val hasUndo = dashboardViewModel.uiState.value.pendingUndo != null
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (hasUndo) "Undo" else null,
                withDismissAction = false
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                dashboardViewModel.undoLastDeletion()
            } else {
                dashboardViewModel.clearPendingUndo()
            }
        }
    }

    LaunchedEffect(Unit) {
        analyticsViewModel.snackbarEvent.collect { message ->
            val hasUndo = analyticsViewModel.hasPendingBudgetUndo()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (hasUndo) "Undo" else null,
                withDismissAction = false
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                analyticsViewModel.undoBudgetDelete()
            } else {
                analyticsViewModel.clearPendingUndo()
            }
        }
    }

    LaunchedEffect(openQuickAdd) {
        if (openQuickAdd) {
            dashboardViewModel.showAdd()
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
            BottomNavBar(
                currentPage = pagerState.currentPage,
                onNavigate = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                onAddClick = { dashboardViewModel.showAdd() },
                showInsightsBadge = showInsightsBadge,
                showHistoryBadge = showHistoryBadge
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(state = pagerState) { page ->
                    val pageOffset = (
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).coerceIn(-1f, 1f)
                    val absOffset = kotlin.math.abs(pageOffset)
                    val scale = 1f - (absOffset * 0.05f)
                    val alpha = 1f - (absOffset * 0.3f)
                    val translationX = pageOffset * -40f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                this.translationX = translationX
                            }
                    ) {
                        when (page) {
                            0 -> DashboardScreen(
                                viewModel = dashboardViewModel,
                                calendarViewModel = calendarViewModel
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
            categories = dashboardState.categories,
            onAddExpense = { amount, category, note, date, recurring ->
                dashboardViewModel.addExpense(amount, category, note, date, recurring)
            },
            onAddIncome = { amount, note, date ->
                dashboardViewModel.addIncome(amount, note, date)
            },
            onAddHabit = { name, icon, targetDays, isNumeric, unit ->
                dashboardViewModel.addHabit(name, icon, targetDays, isNumeric, unit)
            },
            currencySymbol = dashboardState.currency.symbol,
            onAddCategory = dashboardViewModel::addCategory,
            onUpdateCategory = dashboardViewModel::updateCategory,
            onDeleteCategory = dashboardViewModel::deleteCategory
        )
    }

    AnimatedVisibility(
        visible = dashboardState.showCalendarSheet,
        enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
        exit = slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CalendarScreen(viewModel = calendarViewModel)
            androidx.compose.material3.IconButton(
                onClick = { dashboardViewModel.dismissCalendarSheet() },
                modifier = Modifier
                    .padding(start = 16.dp, top = 56.dp)
                    .align(Alignment.TopStart)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    if (dashboardState.showAccountSwitcher) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { dashboardViewModel.dismissAccountSwitcher() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Switch Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                dashboardState.allAccounts.forEach { account ->
                    val isSelected = account.id == dashboardState.currentAccount?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                if (!isSelected) {
                                    dashboardViewModel.switchAccount(account.id)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ehtracker.ui.theme.AccountIcon(
                            icon = account.icon,
                            modifier = Modifier.size(28.dp),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun UnlockLoadingScreen(appName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Unlocking $appName…",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
