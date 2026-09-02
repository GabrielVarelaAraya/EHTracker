package com.example.ehtracker.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

enum class Screen(val label: String, val icon: ImageVector) {
    Dashboard("Home", Icons.Outlined.Home),
    Analytics("Insights", Icons.Outlined.Analytics),
    Logs("History", Icons.Outlined.History)
}

@Composable
fun BottomNavBar(
    currentPage: Int,
    onNavigate: (Int) -> Unit,
    onAddClick: () -> Unit,
    showInsightsBadge: Boolean = false,
    showHistoryBadge: Boolean = false
) {
    Column {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
        Screen.entries.forEachIndexed { index, screen ->
            val selected = index == currentPage
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = tween(300),
                label = "navIconScale"
            )
            val showBadge = when (screen) {
                Screen.Dashboard -> false
                Screen.Analytics -> showInsightsBadge
                Screen.Logs -> showHistoryBadge
            }
            NavigationBarItem(
                icon = {
                    androidx.compose.material3.BadgedBox(
                        badge = {
                            if (showBadge) {
                                androidx.compose.material3.Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            screen.icon,
                            contentDescription = screen.label,
                            modifier = Modifier.size(26.dp).scale(iconScale)
                        )
                    }
                },
                selected = selected,
                onClick = { onNavigate(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(26.dp)
                )
            },
            selected = false,
            onClick = onAddClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
            )
        )
    }
}
}
