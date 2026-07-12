package com.example.ehtracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.ExpenseCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun formatCompact(value: Double, decimals: Int = 1, threshold: Double = 1000.0): String {
    val fmt = "%.${decimals}f"
    val (abbr, suffix) = when {
        value >= 1_000_000 -> fmt.format(value / 1_000_000) to "M"
        value >= threshold -> fmt.format(value / 1_000) to "K"
        else -> return "%.0f".format(value)
    }
    return abbr.trimEnd('0').trimEnd('.') + suffix
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Int = 80,
    strokeWidth: Int = 6,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(progress) {
        animatedProgress = progress
    }
    val animProgress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 800),
        label = "ring"
    )

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val stroke = strokeWidth.dp.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * animProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MiniLineChart(
    data: Map<LocalDate, Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    currencySymbol: String = "$",
    preselectCurrentDay: Boolean = false,
) {
    val sorted = remember(data) { data.entries.sortedBy { it.key } }
    val values = remember(sorted) { sorted.map { it.value } }
    val days = remember(sorted) { sorted.map { it.key } }
    val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }

    var selectedIndex by remember(days) {
        val initial = if (preselectCurrentDay) {
            days.indexOf(LocalDate.now())
        } else -1
        mutableStateOf(initial)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedIndex in days.indices) {
            Text(
                text = "${days[selectedIndex].format(dateFormatter)}: $currencySymbol${formatCompact(values[selectedIndex])}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = lineColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val yAxisWidth = 40.dp
        val chartHeight = 140.dp

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(chartHeight)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$currencySymbol${formatCompact(maxVal)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${formatCompact(maxVal / 2)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${formatCompact(0.0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .pointerInput(values) {
                            detectTapGestures { offset ->
                                if (values.isEmpty()) return@detectTapGestures
                                val chartWidth = size.width
                                val stepX = if (values.size > 1) chartWidth / (values.size - 1) else chartWidth
                                val nearestIndex = ((offset.x) / stepX + 0.5f).toInt()
                                    .coerceIn(0, values.size - 1)
                                selectedIndex = nearestIndex
                            }
                        }
                ) {
                    if (values.isEmpty()) return@Canvas
                    val w = size.width
                    val h = size.height
                    val pad = 4.dp.toPx()
                    val chartH = h - pad * 2

                    for (i in 0..3) {
                        val y = h - pad - (chartH * i / 3)
                        drawLine(
                            color = lineColor.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val pts = values.mapIndexed { index, value ->
                        Offset(
                            x = if (values.size > 1) index * w / (values.size - 1) else 0f,
                            y = h - pad - ((value / maxVal) * chartH).toFloat()
                        )
                    }

                    val fillPath = Path().apply {
                        moveTo(pts.first().x, h)
                        pts.forEach { lineTo(it.x, it.y) }
                        lineTo(pts.last().x, h)
                        close()
                    }
                    drawPath(fillPath, fillColor)

                    val linePath = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        for (i in 1 until pts.size) {
                            lineTo(pts[i].x, pts[i].y)
                        }
                    }
                    drawPath(linePath, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                    val showAllDots = values.size <= 30
                    if (!showAllDots && selectedIndex in pts.indices) {
                        val selPt = pts[selectedIndex]
                        drawLine(
                            color = lineColor.copy(alpha = 0.3f),
                            start = Offset(selPt.x, h - pad),
                            end = Offset(selPt.x, pad),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                        )
                    }
                    pts.forEachIndexed { index, pt ->
                        val isSel = index == selectedIndex
                        if (showAllDots || isSel) {
                            drawCircle(
                                color = if (isSel) Color.White else lineColor,
                                radius = if (isSel) 6.dp.toPx() else 3.dp.toPx(),
                                center = pt
                            )
                            if (isSel) {
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                if (days.isNotEmpty()) {
                    val axisFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labelCount = minOf(days.size, 6)
                        val step = if (days.size > labelCount) days.size / labelCount else 1
                        for (i in days.indices step step.coerceAtLeast(1)) {
                            Text(
                                text = days[i].format(axisFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    totalExpenses: Double,
    totalIncome: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val total = totalExpenses + totalIncome
    val expSweep = if (total > 0) (totalExpenses / total * 360).toFloat() else 0f
    val incSweep = if (total > 0) (totalIncome / total * 360).toFloat() else 0f
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val strokeW = 20.dp.toPx()
            val arcSize = Size(size.width - strokeW, size.height - strokeW)
            val topLeft = Offset(strokeW / 2f, strokeW / 2f)

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = expSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Butt)
            )
            if (incSweep > 0f) {
                drawArc(
                    color = tertiaryColor,
                    startAngle = -90f + expSweep,
                    sweepAngle = incSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${currencySymbol}${formatCompact(total)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expenses: $currencySymbol${formatCompact(totalExpenses)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(tertiaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Income: $currencySymbol${formatCompact(totalIncome)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IncomeVsExpensesChart(
    expenses: Map<LocalDate, Double>,
    incomes: Map<LocalDate, Double>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val allKeys = (expenses.keys + incomes.keys).sorted().toSet()
    val maxVal = maxOf(
        expenses.values.maxOrNull() ?: 1.0,
        incomes.values.maxOrNull() ?: 1.0,
        1.0
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(tertiaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val pad = 4.dp.toPx()
            val chartH = h - pad * 2
            if (allKeys.isEmpty()) return@Canvas
            val barWidth = w / allKeys.size
            allKeys.forEachIndexed { i, day ->
                val expVal = expenses[day] ?: 0.0
                val incVal = incomes[day] ?: 0.0
                val expH = ((expVal / maxVal) * chartH).toFloat()
                val incH = ((incVal / maxVal) * chartH).toFloat()
                val x = i * barWidth + barWidth * 0.15f
                val bw = barWidth * 0.35f
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x, h - pad - expH),
                    size = Size(bw, expH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = tertiaryColor,
                    topLeft = Offset(x + barWidth * 0.5f, h - pad - incH),
                    size = Size(bw, incH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun BudgetVsRealChart(
    budgets: Map<ExpenseCategory, Pair<Double, Double>>,
    actuals: Map<ExpenseCategory, Double>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val categoriesWithBudget = budgets.keys.filter { cat ->
        val (_, limit) = budgets[cat] ?: return@filter false
        limit > 0
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        for (cat in categoriesWithBudget.sortedByDescending { actuals[it] ?: 0.0 }) {
            val (_, limit) = budgets[cat] ?: continue
            val actual = actuals[cat] ?: 0.0
            val fraction = (actual / limit).toFloat().coerceIn(0f, 1f)
            val overBudget = actual > limit

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor
                        )
                        if (overBudget) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Over!",
                                style = MaterialTheme.typography.labelSmall,
                                color = errorColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                        drawRoundRect(
                            color = surfaceVariantColor,
                            size = size,
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        drawRoundRect(
                            color = if (overBudget) errorColor else primaryColor,
                            size = Size(size.width * fraction, size.height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$currencySymbol${"%.0f".format(actual)} / $currencySymbol${"%.0f".format(limit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (overBudget) errorColor else onSurfaceVariantColor
                )
            }
        }
    }
}

@Composable
fun SavingsRateChart(
    data: Map<LocalDate, Double>,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(data) { data.entries.sortedBy { it.key } }
    val values = remember(sorted) { sorted.map { it.value } }
    val days = remember(sorted) { sorted.map { it.key } }
    val maxVal = remember(values) { (values.maxOrNull() ?: 100.0).coerceAtLeast(0.0) }
    val minVal = remember(values) { (values.minOrNull() ?: 0.0).coerceAtMost(0.0) }
    val range = remember(maxVal, minVal) { (maxVal - minVal).coerceAtLeast(1.0) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val zeroLineColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember(days) {
        val initial = if (days.isNotEmpty()) {
            val nowIdx = days.indexOf(LocalDate.now())
            if (nowIdx >= 0) nowIdx else days.lastIndex
        } else -1
        mutableStateOf(initial)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedIndex in days.indices) {
            val rate = values[selectedIndex]
            val rateColor = if (rate >= 0) primaryColor else errorColor
            Text(
                text = "${days[selectedIndex].format(dateFormatter)}: ${"%.1f".format(rate)}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = rateColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val yAxisWidth = 40.dp
        val chartHeight = 140.dp

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(chartHeight)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${"%.0f".format(maxVal)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (minVal < 0) {
                    Text(
                        text = "${"%.0f".format(minVal)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(chartHeight)
                    .pointerInput(values) {
                        detectTapGestures { offset ->
                            if (values.isEmpty()) return@detectTapGestures
                            val chartWidth = size.width
                            val stepX = if (values.size > 1) chartWidth / (values.size - 1) else chartWidth
                            val nearestIndex = ((offset.x) / stepX + 0.5f).toInt()
                                .coerceIn(0, values.size - 1)
                            selectedIndex = nearestIndex
                        }
                    }
            ) {
                if (values.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val pad = 4.dp.toPx()
                val chartH = h - pad * 2

                val zeroY = h - pad - ((0.0 - minVal) / range * chartH).toFloat()

                drawLine(
                    color = zeroLineColor.copy(alpha = 0.4f),
                    start = Offset(0f, zeroY),
                    end = Offset(w, zeroY),
                    strokeWidth = 1.dp.toPx()
                )

                for (i in 0..3) {
                    val y = h - pad - (chartH * i / 3)
                    drawLine(
                        color = zeroLineColor.copy(alpha = 0.06f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val pts = values.mapIndexed { index, value ->
                    Offset(
                        x = if (values.size > 1) index * w / (values.size - 1) else 0f,
                        y = h - pad - ((value - minVal) / range * chartH).toFloat()
                    )
                }

                val fillAbove = Path().apply {
                    moveTo(pts.first().x, zeroY)
                    pts.forEach { pt ->
                        lineTo(pt.x, pt.y.coerceAtMost(zeroY))
                    }
                    lineTo(pts.last().x, zeroY)
                    close()
                }
                drawPath(fillAbove, primaryColor.copy(alpha = 0.05f))

                val fillBelow = Path().apply {
                    moveTo(pts.first().x, zeroY)
                    pts.forEach { pt ->
                        lineTo(pt.x, pt.y.coerceAtLeast(zeroY))
                    }
                    lineTo(pts.last().x, zeroY)
                    close()
                }
                drawPath(fillBelow, errorColor.copy(alpha = 0.05f))

                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
                }
                drawPath(linePath, primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                val showAllDots = values.size <= 30
                if (!showAllDots && selectedIndex in pts.indices) {
                    val selPt = pts[selectedIndex]
                    drawLine(
                        color = primaryColor.copy(alpha = 0.3f),
                        start = Offset(selPt.x, h - pad),
                        end = Offset(selPt.x, pad),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                    )
                }
                pts.forEachIndexed { index, pt ->
                    val isSel = index == selectedIndex
                    if (showAllDots || isSel) {
                        drawCircle(
                            color = if (isSel) Color.White else primaryColor,
                            radius = if (isSel) 6.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                        if (isSel) {
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }

        if (days.isNotEmpty()) {
            val axisFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = yAxisWidth + 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelCount = minOf(days.size, 6)
                val step = if (days.size > labelCount) days.size / labelCount else 1
                for (i in days.indices step step.coerceAtLeast(1)) {
                    Text(
                        text = days[i].format(axisFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBar(
    label: String,
    amount: Double,
    fraction: Float,
    color: Color,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(70.dp)
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        ) {
            drawRoundRect(
                color = color.copy(alpha = 0.15f),
                size = size,
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = color,
                size = Size(size.width * fraction, size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$currencySymbol${formatCompact(amount, decimals = 3, threshold = 100_000.0)}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
