package com.example.financemanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.BorrowLend
import com.example.financemanager.model.BorrowLendType
import com.example.financemanager.ui.components.BorrowLendDetailsDialog
import com.example.financemanager.ui.components.SwipeableBorrowLendItem
import com.example.financemanager.ui.utils.swipeBackGesture
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivablesScreen(
    onBackClick: () -> Unit,
    onAddRecordClick: () -> Unit,
    onEditRecordClick: (BorrowLend) -> Unit,
    viewModel: FinanceViewModel = viewModel(),
    scrollState: LazyListState = rememberLazyListState()
) {
    val records by viewModel.allBorrowLendRecords.collectAsState()
    val totalReceivable by viewModel.totalReceivable.collectAsState()

    val receivableRecords = records.filter {
        it.type == BorrowLendType.LENT && !it.isSettled
    }

    // Sorting and grouping state
    var selectedRecord by remember { mutableStateOf<BorrowLend?>(null) }
    var sortOption by remember { mutableStateOf("date_desc") }
    var groupByPerson by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    // Process records based on sorting and grouping
    val processedRecords = remember(receivableRecords, sortOption, groupByPerson) {
        processBorrowLendRecords(receivableRecords, sortOption, groupByPerson)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .swipeBackGesture(onBack = onBackClick), // Use the gesture modifier
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "People Owe You",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sort and group dropdown
                    Box {
                        IconButton(onClick = { sortExpanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort and Group")
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            // Sorting options
                            DropdownMenuItem(
                                text = { Text("Sort by Date (Newest)") },
                                onClick = {
                                    sortOption = "date_desc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Date (Oldest)") },
                                onClick = {
                                    sortOption = "date_asc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Amount (High to Low)") },
                                onClick = {
                                    sortOption = "amount_desc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Amount (Low to High)") },
                                onClick = {
                                    sortOption = "amount_asc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Person (A-Z)") },
                                onClick = {
                                    sortOption = "person_asc"
                                    sortExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Person (Z-A)") },
                                onClick = {
                                    sortOption = "person_desc"
                                    sortExpanded = false
                                }
                            )

                            Divider()

                            // Grouping options
                            DropdownMenuItem(
                                text = {
                                    Text(if (groupByPerson) "✓ Group by Person" else "Group by Person")
                                },
                                onClick = {
                                    groupByPerson = !groupByPerson
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecordClick,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Receivable")
            }
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
                            text = "Total Receivable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(totalReceivable ?: 0.0),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${receivableRecords.size} records",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${receivableRecords.distinctBy { it.personName }.size} people",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sorting and Grouping Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = getSortDescription(sortOption),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (groupByPerson) {
                            Text(
                                text = "Grouped by person",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "${processedRecords.size} ${if (groupByPerson) "people" else "records"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Records List
            if (receivableRecords.isEmpty()) {
                EmptyReceivablesState(
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
                    if (groupByPerson && processedRecords.isNotEmpty() && processedRecords[0] is PersonGroup) {
                        // Grouped by person
                        items(processedRecords.filterIsInstance<PersonGroup>()) { group ->
                            PersonGroupSection(
                                group = group,
                                onEditRecordClick = onEditRecordClick,
                                onRecordClick = { selectedRecord = it },
                                onDeleteRecord = { viewModel.deleteBorrowLendRecord(it) }
                            )
                        }
                    } else {
                        // Not grouped - flat list
                        items(processedRecords.filterIsInstance<BorrowLend>()) { record ->
                            SwipeableBorrowLendItem(
                                record = record,
                                onDelete = { viewModel.deleteBorrowLendRecord(record) },
                                onEdit = { onEditRecordClick(record) },
                                onClick = { selectedRecord = record },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Details Dialog
        selectedRecord?.let { record ->
            BorrowLendDetailsDialog(
                record = record,
                onDismiss = { selectedRecord = null },
                onEdit = {
                    onEditRecordClick(record)
                    selectedRecord = null
                },
                onDelete = {
                    viewModel.deleteBorrowLendRecord(record)
                    selectedRecord = null
                },
                onToggleSettle = {
                    val updatedRecord = record.copy(
                        isSettled = !record.isSettled,
                        settledDate = if (!record.isSettled) Date() else null
                    )
                    viewModel.updateBorrowLendRecord(updatedRecord)
                    selectedRecord = null
                }
            )
        }
    }
}

// Data classes for grouping
sealed class ProcessedRecord
data class PersonGroup(val personName: String, val totalAmount: Double, val records: List<BorrowLend>) : ProcessedRecord()

// Processing function
fun processBorrowLendRecords(
    records: List<BorrowLend>,
    sortOption: String,
    groupByPerson: Boolean
): List<ProcessedRecord> {
    val sortedRecords = when (sortOption) {
        "date_asc" -> records.sortedBy { it.date }
        "amount_desc" -> records.sortedByDescending { it.amount }
        "amount_asc" -> records.sortedBy { it.amount }
        "person_asc" -> records.sortedBy { it.personName.lowercase() }
        "person_desc" -> records.sortedByDescending { it.personName.lowercase() }
        else -> records.sortedByDescending { it.date } // date_desc
    }

    return (if (groupByPerson) {
        sortedRecords
            .groupBy { it.personName }
            .map { (personName, personRecords) ->
                PersonGroup(
                    personName = personName,
                    totalAmount = personRecords.sumOf { it.amount },
                    records = personRecords.sortedByDescending { it.date } // Within group, sort by date
                )
            }
            .sortedByDescending { it.totalAmount } // Sort groups by total amount
    } else {
        sortedRecords
    }) as List<ProcessedRecord>
}

// Group section composable
@Composable
private fun PersonGroupSection(
    group: PersonGroup,
    onEditRecordClick: (BorrowLend) -> Unit,
    onRecordClick: (BorrowLend) -> Unit,
    onDeleteRecord: (BorrowLend) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Group header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = group.personName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${group.records.size} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "+${formatCurrency(group.totalAmount)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Group items
        group.records.forEach { record ->
            SwipeableBorrowLendItem(
                record = record,
                onDelete = { onDeleteRecord(record) },
                onEdit = { onEditRecordClick(record) },
                onClick = { onRecordClick(record) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmptyReceivablesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Money,
            contentDescription = "No receivables",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Receivables",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "People who owe you money will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun getSortDescription(sortOption: String): String {
    return when (sortOption) {
        "date_desc" -> "Sorted by: Date (Newest First)"
        "date_asc" -> "Sorted by: Date (Oldest First)"
        "amount_desc" -> "Sorted by: Amount (High to Low)"
        "amount_asc" -> "Sorted by: Amount (Low to High)"
        "person_asc" -> "Sorted by: Person (A to Z)"
        "person_desc" -> "Sorted by: Person (Z to A)"
        else -> "Sorted by: Date (Newest First)"
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("HKD")
    return format.format(amount)
}