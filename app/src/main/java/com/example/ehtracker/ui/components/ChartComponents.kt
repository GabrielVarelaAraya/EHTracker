package com.example.ehtracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Progress
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
    data: Map<Int, Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    maxLabel: String = "Max",
) {
    val values = data.values.toList()
    val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val days = data.keys.toList()

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            if (values.isEmpty()) return@Canvas
            val width = size.width
            val height = size.height
            val stepX = if (values.size > 1) width / (values.size - 1) else width
            val padding = 4.dp.toPx()

            val points = values.mapIndexed { index, value ->
                Offset(
                    x = index * stepX,
                    y = height - padding - ((value / maxVal) * (height - padding * 2)).toFloat()
                )
            }

            // Fill path
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(fillPath, fillColor)

            // Line path
            val linePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(linePath, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // Dots
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }

        // Day labels
        if (days.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelCount = minOf(days.size, 6)
                val step = if (days.size > labelCount) days.size / labelCount else 1
                for (i in days.indices step step.coerceAtLeast(1)) {
                    Text(
                        text = "${days[i]}",
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
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = color,
                size = Size(size.width * fraction, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$currencySymbol${"%.0f".format(amount)}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(40.dp)
        )
    }
}
