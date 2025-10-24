package com.example.financemanager.ui.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.ThemeManager
import com.example.financemanager.model.Transaction
import com.example.financemanager.ui.components.SwipeableTransactionItem
import com.example.financemanager.ui.components.TransactionDetailsDialog
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TransactionsScreen(
    onEditTransaction: (Transaction) -> Unit,
    viewModel: FinanceViewModel = viewModel(),
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState()
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val isDarkTheme = remember { ThemeManager.isDarkTheme }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Sorting state
    var sortOrder by remember { mutableStateOf("date_desc") } // date_desc, date_asc, amount_desc, amount_asc, category
    var sortExpanded by remember { mutableStateOf(false) }

    // Calculate summary stats
    val totalIncome = viewModel.calculateTotalIncome(transactions)
    val totalExpense = viewModel.calculateTotalExpense(transactions)
    val netBalance = totalIncome - totalExpense
    val transactionCount = transactions.size

    // Sort transactions based on current sort order
    val sortedTransactions = when (sortOrder) {
        "date_asc" -> transactions.sortedBy { it.date }
        "amount_desc" -> transactions.sortedByDescending { it.amount }
        "amount_asc" -> transactions.sortedBy { it.amount }
        "category" -> transactions.sortedBy { it.category.name }
        else -> transactions.sortedByDescending { it.date } // date_desc
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "All Transactions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                actions = {
                    // Theme toggle
                    IconButton(
                        onClick = { ThemeManager.toggleTheme() }
                    ) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = isDarkTheme,
                            transitionSpec = {
                                slideInVertically { height -> height } with
                                        slideOutVertically { height -> -height }
                            }
                        ) { targetDarkTheme ->
                            Icon(
                                imageVector = if (targetDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Toggle Theme",
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }

                    // Sort dropdown
                    Box {
                        IconButton(onClick = { sortExpanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort Transactions")
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            // Date sorting
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Date (Newest First)")
                                    }
                                },
                                onClick = {
                                    sortOrder = "date_desc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Date (Oldest First)")
                                    }
                                },
                                onClick = {
                                    sortOrder = "date_asc"
                                    sortExpanded = false
                                }
                            )

                            Divider()

                            // Amount sorting
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Amount (High to Low)")
                                    }
                                },
                                onClick = {
                                    sortOrder = "amount_desc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Amount (Low to High)")
                                    }
                                },
                                onClick = {
                                    sortOrder = "amount_asc"
                                    sortExpanded = false
                                }
                            )

                            Divider()

                            // Category sorting
                            DropdownMenuItem(
                                text = { Text("Sort by Category") },
                                onClick = {
                                    sortOrder = "category"
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total",
                    value = transactionCount.toString(),
                    subtitle = "transactions",
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )

                SummaryCard(
                    title = "Net Balance",
                    value = formatCurrency(netBalance),
                    subtitle = if (netBalance >= 0) "Profit" else "Loss",
                    isPositive = netBalance >= 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )
            }

            // Income vs Expense Overview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Income",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${formatCurrency(totalIncome)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Expense",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-${formatCurrency(totalExpense)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transactions List Header with Sort Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "All Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getSortDescription(sortOrder),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$transactionCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transactions List
            if (sortedTransactions.isEmpty()) {
                EmptyTransactionsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(sortedTransactions) { transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) },
                            onEdit = { onEditTransaction(transaction) },
                            onClick = { selectedTransaction = transaction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        // Transaction Details Dialog
        selectedTransaction?.let { transaction ->
            TransactionDetailsDialog(
                transaction = transaction,
                onDismiss = { selectedTransaction = null },
                onEdit = {
                    onEditTransaction(transaction)
                    selectedTransaction = null
                },
                onDelete = {
                    viewModel.deleteTransaction(transaction)
                    selectedTransaction = null
                }
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isPositive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyTransactionsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No Transactions",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your transactions will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Helper function to get sort description
private fun getSortDescription(sortOrder: String): String {
    return when (sortOrder) {
        "date_desc" -> "Sorted by: Date (Newest First)"
        "date_asc" -> "Sorted by: Date (Oldest First)"
        "amount_desc" -> "Sorted by: Amount (High to Low)"
        "amount_asc" -> "Sorted by: Amount (Low to High)"
        "category" -> "Sorted by: Category"
        else -> "Sorted by: Date (Newest First)"
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = java.util.Currency.getInstance("HKD")
    return format.format(amount)
}