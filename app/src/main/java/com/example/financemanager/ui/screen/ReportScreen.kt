package com.example.financemanager.ui.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.FinancialReport
import com.example.financemanager.model.FinancialSummary
import com.example.financemanager.model.ReportPeriod
import com.example.financemanager.ui.components.*
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBackClick: () -> Unit,
    viewModel: FinanceViewModel = viewModel(),
    scrollState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTHLY) }
    val report by remember(selectedPeriod) {
        derivedStateOf { viewModel.generateFinancialReport(period = selectedPeriod) }
    }

    BackHandler { onBackClick() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (change.position.x < (size.width * 0.15) && dragAmount > 80f) {
                        onBackClick()
                    }
                }
            },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Financial Insights",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareFinancialReport(context, report)
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Share Report")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { innerPadding ->
        // FIXED: Remove the Column wrapper and use LazyColumn directly
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Period Selector
            item {
                ModernPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp)
                )
            }

            // Report Header with gradient background
            item {
                ModernReportHeader(
                    period = report.period,
                    generatedDate = report.generatedDate,
                    netBalance = report.summary.netBalance
                )
            }

            // Quick Stats Grid
            item {
                QuickStatsGrid(summary = report.summary)
            }

            // AI Insights Section
            if (report.insights.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "AI Insights",
                        icon = Icons.Default.Insights,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(report.insights) { insight ->
                    ModernFinancialInsightCard(insight = insight)
                }
            }

            // Spending Breakdown
            item {
                SectionHeader(
                    title = "Spending Analysis",
                    icon = Icons.Default.PieChart,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            item {
                ModernSpendingAnalysis(
                    summary = report.summary,
                    transactions = report.transactions
                )
            }

            // Wallet Overview
            item {
                SectionHeader(
                    title = "Wallet Overview",
                    icon = Icons.Default.Wallet,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            item {
                ModernWalletOverview(walletBalances = report.summary.walletBalances)
            }

            // Borrow & Lend Summary
            item {
                SectionHeader(
                    title = "Borrow & Lend",
                    icon = Icons.Default.Receipt,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ModernBorrowLendSummary(
                    receivables = report.receivables,
                    repayables = report.repayables,
                    totalReceivable = report.summary.totalReceivable,
                    totalRepayable = report.summary.totalRepayable
                )
            }

            // Recent Transactions
            item {
                SectionHeader(
                    title = "Recent Activity",
                    icon = Icons.Default.Receipt,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                ModernRecentTransactions(transactions = report.transactions.take(5))
            }
        }
    }
}

@Composable
private fun ModernPeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remove CUSTOM from the list
    val periods = listOf(ReportPeriod.WEEKLY, ReportPeriod.MONTHLY, ReportPeriod.QUARTERLY, ReportPeriod.YEARLY)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                FilterChip(
                    selected = isSelected,
                    onClick = { onPeriodSelected(period) },
                    label = {
                        Text(
                            period.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernReportHeader(
    period: ReportPeriod,
    generatedDate: Date,
    netBalance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated balance indicator
            Text(
                text = formatCurrency(netBalance),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = if (netBalance >= 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )

            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${period.displayName} Financial Report",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Generated ${SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(generatedDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun QuickStatsGrid(summary: FinancialSummary) {
    val stats = listOf(
        StatItem("Income", "+${formatCurrency(summary.totalIncome)}", MaterialTheme.colorScheme.primary),
        StatItem("Expense", "-${formatCurrency(summary.totalExpense)}", MaterialTheme.colorScheme.error),
        StatItem("Savings Rate", "${summary.savingsRate.format(1)}%",
            when {
                summary.savingsRate > 20 -> MaterialTheme.colorScheme.primary
                summary.savingsRate > 0 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
        ),
        StatItem("Cash Flow", formatCurrency(summary.netBalance),
            if (summary.netBalance >= 0) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    )

    // FIXED: Use Column instead of LazyColumn to avoid nested scrolling
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { stat ->
                    QuickStatCard(
                        title = stat.title,
                        value = stat.value,
                        color = stat.color,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Share function for financial report
private fun shareFinancialReport(context: Context, report: FinancialReport) {
    val shareText = buildString {
        appendLine("📊 ${report.period.displayName} Financial Report")
        appendLine("Generated on ${SimpleDateFormat("MMM dd, yyyy").format(report.generatedDate)}")
        appendLine()
        appendLine("💰 Financial Summary:")
        appendLine("• Income: +${formatCurrency(report.summary.totalIncome)}")
        appendLine("• Expense: -${formatCurrency(report.summary.totalExpense)}")
        appendLine("• Net Balance: ${formatCurrency(report.summary.netBalance)}")
        appendLine("• Savings Rate: ${report.summary.savingsRate.format(1)}%")
        appendLine()
        appendLine("💡 Key Insights:")
        report.insights.take(3).forEach { insight ->
            appendLine("• ${insight.title}: ${insight.message}")
        }
        appendLine()
        appendLine("--- Generated by Finance Manager ---")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Financial Report"))
}

private fun formatCurrency(amount: Double): String {
    val format = java.text.NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("HKD")
    return format.format(amount)
}

private fun Double.format(digits: Int): String = "%.${digits}f".format(this)

// Helper data class for stats
private data class StatItem(
    val title: String,
    val value: String,
    val color: androidx.compose.ui.graphics.Color
)