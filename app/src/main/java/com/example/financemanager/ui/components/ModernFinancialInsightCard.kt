package com.example.financemanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.FinancialInsight
import com.example.financemanager.model.InsightSeverity

// Data class to hold all insight styling properties
private data class InsightStyle(
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color,
    val borderColor: Color
)

@Composable
fun ModernFinancialInsightCard(insight: FinancialInsight) {
    val insightStyle = when (insight.severity) {
        InsightSeverity.POSITIVE -> InsightStyle(
            icon = Icons.Default.CheckCircle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        InsightSeverity.NEUTRAL -> InsightStyle(
            icon = Icons.Default.Info,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        InsightSeverity.WARNING -> InsightStyle(
            icon = Icons.Default.Warning,
            containerColor = Color(0xFFFFF3CD),
            iconColor = Color(0xFF856404),
            borderColor = Color(0xFFFFC107).copy(alpha = 0.3f)
        )
        InsightSeverity.CRITICAL -> InsightStyle(
            icon = Icons.Default.Error,
            containerColor = Color(0xFFF8D7DA),
            iconColor = Color(0xFF721C24),
            borderColor = Color(0xFFDC3545).copy(alpha = 0.3f)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = insightStyle.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder(true),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = insightStyle.icon,
                contentDescription = null,
                tint = insightStyle.iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
                insight.recommendation?.let { recommendation ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "💡",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}