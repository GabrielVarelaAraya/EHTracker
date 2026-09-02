package com.example.ehtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HabitProgressChart(
    data: List<Pair<LocalDate, Double>>,
    isNumeric: Boolean,
    unit: String,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    gridColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    if (data.isEmpty()) return

    val values = data.map { it.second }
    val dates = data.map { it.first }

    // For binary, y in 0..1. For numeric, y in min..max (no zero, only recorded points)
    val maxVal: Double
    val minVal: Double
    val range: Double
    if (!isNumeric) {
        maxVal = 1.0
        minVal = 0.0
        range = 1.0
    } else {
        val rawMax = values.maxOrNull() ?: 0.0
        val rawMin = values.minOrNull() ?: 0.0
        // usar max+1 y min-1 para que no quede a ras, como pidió el usuario
        maxVal = rawMax + 1
        minVal = rawMin - 1
        range = (maxVal - minVal).coerceAtLeast(1.0)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
    val yLabelFormatter: (Double) -> String = { v ->
        if (isNumeric) {
            if (v >= 10) "%.0f".format(v) else "%.1f".format(v).trimEnd('0').trimEnd('.')
        } else {
            if (v == 1.0) "1" else "0"
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val yAxisWidth = 36.dp
        val chartHeight = 160.dp

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(chartHeight)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                if (!isNumeric) {
                    Text(text = "1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(text = "0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(text = yLabelFormatter(maxVal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = yLabelFormatter((maxVal + minVal) / 2), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = yLabelFormatter(minVal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                ) {
                    val w = size.width
                    val h = size.height
                    val pad = 4.dp.toPx()
                    val hPad = 14.dp.toPx()
                    val chartH = h - pad * 2
                    val chartW = (w - hPad * 2).coerceAtLeast(0f)

                    // grid
                    for (i in 0..3) {
                        val y = h - pad - (chartH * i / 3)
                        drawLine(
                            color = gridColor,
                            start = Offset(hPad, y),
                            end = Offset(w - hPad, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val pts = values.mapIndexed { index, value ->
                        Offset(
                            x = if (values.size > 1) hPad + index * chartW / (values.size - 1) else w / 2,
                            y = h - pad - ((value - minVal) / range * chartH).toFloat()
                        )
                    }

                    // lollipop stems
                    pts.forEach { pt ->
                        drawLine(
                            color = lineColor.copy(alpha = 0.35f),
                            start = Offset(pt.x, h - pad),
                            end = Offset(pt.x, pt.y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // trace line between points
                    if (pts.size > 1) {
                        val linePath = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                        }
                        drawPath(linePath, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }

                    // lollipop heads + labels (with decimals)
                    pts.forEachIndexed { idx, pt ->
                        if (isNumeric) {
                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
                            drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = pt)
                            // label with decimals
                            val raw = values[idx]
                            val label = "%.2f".format(raw).trimEnd('0').trimEnd('.')
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(
                                    (lineColor.alpha * 255).toInt(),
                                    (lineColor.red * 255).toInt(),
                                    (lineColor.green * 255).toInt(),
                                    (lineColor.blue * 255).toInt()
                                )
                                textSize = 10.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            val yPos = (pt.y - 18.9.dp.toPx()).coerceAtLeast(paint.textSize + 2.dp.toPx())
                            drawContext.canvas.nativeCanvas.drawText(label, pt.x, yPos, paint)
                        } else {
                            val v = values[idx]
                            if (v >= 1.0) {
                                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
                                drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = pt)
                            } else {
                                drawCircle(color = gridColor, radius = 4.dp.toPx(), center = pt)
                                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // show first, mid, last
                    val indices = when {
                        dates.size <= 3 -> dates.indices.toList()
                        else -> listOf(0, dates.size / 2, dates.size - 1)
                    }
                    for (i in indices) {
                        Text(
                            text = dates[i].format(dateFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (isNumeric && unit.isNotBlank()) {
            Text(
                text = "Unit: $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
