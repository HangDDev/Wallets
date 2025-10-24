package com.example.financemanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.Category
import com.example.financemanager.model.PaymentMethod
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.getCategoryDisplayName
import com.example.financemanager.ui.utils.swipeBackGesture
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    transactionToEdit: Transaction? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var selectedCategory by remember {
        mutableStateOf(transactionToEdit?.category ?: Category.MEAL)
    }
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var isExpense by remember { mutableStateOf(transactionToEdit?.isExpense ?: true) }
    var paymentMethod by remember {
        mutableStateOf(transactionToEdit?.paymentMethod ?: PaymentMethod.CASH)
    }

    // Category dropdown state
    var categoryExpanded by remember { mutableStateOf(false) }

    val screenTitle = if (transactionToEdit != null) "Edit Transaction" else "Add Transaction"
    val buttonText = if (transactionToEdit != null) "Update Transaction" else "Save Transaction"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onBack = onBackClick), // Use the gesture modifier
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        screenTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = isExpense,
                            onClick = { isExpense = true },
                            label = { Text("Expense") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isExpense,
                            onClick = { isExpense = false },
                            label = { Text("Income") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Method Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wallets",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = {
                                    Text(
                                        when (method) {
                                            PaymentMethod.CASH -> "Cash"
                                            PaymentMethod.ALIPAY -> "Alipay"
                                            PaymentMethod.OCTOPUS -> "Octopus"
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transaction Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = amount.isNotBlank() && amount.toDoubleOrNull() == null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selection - FIXED with ExposedDropdownMenuBox
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = getCategoryDisplayName(selectedCategory),
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            Category.values().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            getCategoryDisplayName(category),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Add a note...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save/Update Button
            Button(
                onClick = {
                    val transactionAmount = amount.toDoubleOrNull()
                    if (transactionAmount != null && transactionAmount > 0) {
                        val transaction = if (transactionToEdit != null) {
                            transactionToEdit.copy(
                                amount = transactionAmount,
                                category = selectedCategory,
                                description = description,
                                isExpense = isExpense,
                                paymentMethod = paymentMethod
                            )
                        } else {
                            Transaction(
                                amount = transactionAmount,
                                category = selectedCategory,
                                description = description,
                                date = Date(),
                                isExpense = isExpense,
                                paymentMethod = paymentMethod
                            )
                        }

                        if (transactionToEdit != null) {
                            viewModel.deleteTransaction(transactionToEdit)
                        }
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = amount.isNotBlank() &&
                        amount.toDoubleOrNull() != null &&
                        amount.toDoubleOrNull() ?: 0.0 > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    buttonText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            if (transactionToEdit != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}