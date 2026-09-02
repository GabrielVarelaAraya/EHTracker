package com.example.ehtracker.ui.widget

import android.content.Context
import android.content.Intent
import com.example.ehtracker.util.formatMoney
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.ehtracker.EHTrackerApplication
import com.example.ehtracker.MainActivity
import kotlinx.coroutines.flow.first

class BalanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as EHTrackerApplication).repository

        var accountName = ""
        var symbol = "$"
        var balance = 0.0
        var monthExpenses = 0.0
        try {
            accountName = repository.currentAccount().first()?.name ?: ""
            symbol = repository.currency().first().symbol
            balance = repository.currentBalance().first()
            monthExpenses = repository.thisMonthExpenses().first()
        } catch (e: Exception) {
            // Keep defaults so the widget never crashes on missing data.
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFFF5F5F5)))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(12),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.Top
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = accountName.ifBlank { "EHTracker" },
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF6750A4))
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                Spacer(modifier = GlanceModifier.height(4))
                Text(
                    text = "$symbol${formatMoney(balance)}",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF1C1B1F))
                    )
                )
                Spacer(modifier = GlanceModifier.height(2))
                Text(
                    text = "This month: -$symbol${formatMoney(monthExpenses)}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(androidx.compose.ui.graphics.Color(0xFFB3261E))
                    )
                )
                Spacer(modifier = GlanceModifier.height(8))
                FilledButton(
                    text = "Add expense",
                    onClick = actionRunCallback<OpenQuickAddAction>(),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
        }
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}

class OpenQuickAddAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_QUICK_ADD, true)
        }
        context.startActivity(intent)
    }
}