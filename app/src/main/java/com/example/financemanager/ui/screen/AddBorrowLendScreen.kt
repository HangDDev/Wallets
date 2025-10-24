package com.example.financemanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.BorrowLend
import com.example.financemanager.model.BorrowLendType
import com.example.financemanager.ui.utils.swipeBackGesture
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBorrowLendScreen(
    onBackClick: () -> Unit,
    recordToEdit: BorrowLend? = null,
    // ADD THIS: Default type parameter
    defaultType: BorrowLendType? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    var personName by remember { mutableStateOf(recordToEdit?.personName ?: "") }
    var amount by remember { mutableStateOf(recordToEdit?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(recordToEdit?.description ?: "") }
    // MODIFY THIS: Use defaultType if provided, otherwise use recordToEdit's type
    var selectedType by remember {
        mutableStateOf(
            recordToEdit?.type ?: defaultType ?: BorrowLendType.LENT
        )
    }
    var dueDate by remember { mutableStateOf(recordToEdit?.dueDate ?: Date()) }

    // MODIFY THIS: Dynamic screen title based on type
    val screenTitle = if (recordToEdit != null) {
        "Edit Record"
    } else {
        when (selectedType) {
            BorrowLendType.LENT -> "Add Receivable"
            BorrowLendType.BORROWED -> "Add Repayable"
        }
    }

    val buttonText = if (recordToEdit != null) "Update Record" else "Save Record"

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
            // Record Type Selection - HIDE IF DEFAULT TYPE IS PROVIDED
            if (defaultType == null) {
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
                                selected = selectedType == BorrowLendType.LENT,
                                onClick = { selectedType = BorrowLendType.LENT },
                                label = { Text("I Lent Money") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedType == BorrowLendType.BORROWED,
                                onClick = { selectedType = BorrowLendType.BORROWED },
                                label = { Text("I Borrowed Money") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Record Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Record Details",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Person Name
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    BorrowLendType.LENT -> "Who owes you money?"
                                    BorrowLendType.BORROWED -> "Who did you borrow from?"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Person")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount
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

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    BorrowLendType.LENT -> "What was the money for? (Optional)"
                                    BorrowLendType.BORROWED -> "What did you borrow for? (Optional)"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (selectedType) {
                                    BorrowLendType.LENT -> "e.g., Lunch money, Rent, etc."
                                    BorrowLendType.BORROWED -> "e.g., Emergency, Shopping, etc."
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save/Update Button
            Button(
                onClick = {
                    val recordAmount = amount.toDoubleOrNull()
                    if (recordAmount != null && recordAmount > 0 && personName.isNotBlank()) {
                        val record = if (recordToEdit != null) {
                            recordToEdit.copy(
                                personName = personName,
                                amount = recordAmount,
                                description = description,
                                type = selectedType,
                                dueDate = dueDate
                            )
                        } else {
                            BorrowLend(
                                personName = personName,
                                amount = recordAmount,
                                description = description,
                                type = selectedType,
                                date = Date(),
                                dueDate = dueDate,
                                isSettled = false
                            )
                        }

                        if (recordToEdit != null) {
                            viewModel.updateBorrowLendRecord(record)
                        } else {
                            viewModel.addBorrowLendRecord(record)
                        }
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = personName.isNotBlank() &&
                        amount.isNotBlank() &&
                        amount.toDoubleOrNull() != null &&
                        amount.toDoubleOrNull() ?: 0.0 > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        BorrowLendType.LENT -> MaterialTheme.colorScheme.primary
                        BorrowLendType.BORROWED -> MaterialTheme.colorScheme.error
                    }
                )
            ) {
                Text(
                    buttonText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            if (recordToEdit != null) {
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