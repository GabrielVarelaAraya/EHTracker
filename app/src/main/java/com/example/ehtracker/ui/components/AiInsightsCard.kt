package com.example.ehtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ehtracker.ui.analytics.AiInsight
import com.example.ehtracker.ui.analytics.InsightType

@Composable
fun AiInsightsCard(
    insights: List<AiInsight>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = "Insights",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        insights.forEachIndexed { index, insight ->
            val bullet = when (insight.type) {
                InsightType.SAVING -> "→"
                InsightType.TIP -> "•"
                InsightType.WARNING -> "!"
            }
            val color = when (insight.type) {
                InsightType.SAVING -> MaterialTheme.colorScheme.primary
                InsightType.TIP -> MaterialTheme.colorScheme.onSurfaceVariant
                InsightType.WARNING -> MaterialTheme.colorScheme.error
            }

            Text(
                text = "$bullet ${insight.text}",
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            if (index < insights.lastIndex) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}
