package com.example.financemanager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.Transaction
import com.example.financemanager.ui.components.SwipeableTransactionItem
import com.example.financemanager.ui.components.TransactionDetailsDialog
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredTransactionsScreen(
    transactionType: String, // "income" or "expense"
    onBackClick: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    viewModel: FinanceViewModel = viewModel(),
    scrollState: LazyListState = rememberLazyListState()
) {
    val transactions by viewModel.allTransactions.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var sortOrder by remember { mutableStateOf("date") } // "date", "amount", "category"

    // Handle back button and gestures
    BackHandler {
        onBackClick()
    }

    val filteredTransactions = remember(transactions, transactionType, sortOrder) {
        when (transactionType) {
            "income" -> transactions.filter { !it.isExpense }
            "expense" -> transactions.filter { it.isExpense }
            else -> transactions
        }.sortedWith(
            when (sortOrder) {
                "amount" -> compareByDescending<Transaction> { it.amount }
                "category" -> compareBy<Transaction> { it.category.name }
                else -> compareByDescending<Transaction> { it.date }
            }
        )
    }

    val totalAmount = filteredTransactions.sumOf { it.amount }
    val transactionCount = filteredTransactions.size

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    // Swipe from left edge with threshold
                    if (change.position.x < 100f && dragAmount > 100f) {
                        onBackClick()
                    }
                }
            },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (transactionType == "income") "Income Transactions" else "Expense Transactions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sort dropdown
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Date") },
                                onClick = {
                                    sortOrder = "date"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Amount") },
                                onClick = {
                                    sortOrder = "amount"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Category") },
                                onClick = {
                                    sortOrder = "category"
                                    expanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total ${if (transactionType == "income") "Income" else "Expense"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (transactionType == "income")
                                "+${formatCurrency(totalAmount)}"
                            else
                                "-${formatCurrency(totalAmount)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (transactionType == "income")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "$transactionCount transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transactions List
            Text(
                text = "Transactions (${if (sortOrder == "date") "Latest First" else "Sorted by " + sortOrder.replaceFirstChar { it.uppercase() }})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTransactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No ${if (transactionType == "income") "Income" else "Expense"} Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${if (transactionType == "income") "Income" else "Expense"} transactions will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTransactions) { transaction ->
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

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = java.util.Currency.getInstance("HKD")
    return format.format(amount)
}