package com.example.financemanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.FinancialSummary
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.getCategoryDisplayName
import java.text.NumberFormat

@Composable
fun ModernSpendingAnalysis(
    summary: FinancialSummary,
    transactions: List<Transaction>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top Category Highlight
            summary.topSpendingCategory?.let { category ->
                val percentage = (summary.topSpendingAmount / summary.totalExpense * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Top Category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getCategoryDisplayName(category),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "-${formatCurrency(summary.topSpendingAmount)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "$percentage% of expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Breakdown
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                val topCategories = summary.categoryBreakdown.toList()
                    .sortedByDescending { it.second }
                    .take(5)

                topCategories.forEach { (category, amount) ->
                    CategorySpendingRow(
                        category = category,
                        amount = amount,
                        percentage = (amount / summary.totalExpense * 100).toInt(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategorySpendingRow(
    category: com.example.financemanager.model.Category,
    amount: Double,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getCategoryDisplayName(category),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "-${formatCurrency(amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = java.util.Currency.getInstance("HKD")
    return format.format(amount)
}