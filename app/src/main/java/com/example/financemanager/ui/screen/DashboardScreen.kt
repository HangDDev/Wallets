package com.example.financemanager.ui.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.ThemeManager
import com.example.financemanager.model.PaymentMethod
import com.example.financemanager.model.Transaction
import com.example.financemanager.ui.components.ExpensePieChart
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import java.text.NumberFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    onAddTransactionClick: () -> Unit,
    onWalletClick: (PaymentMethod) -> Unit,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onReceivablesClick: () -> Unit,
    onRepayablesClick: () -> Unit,
    onGenerateReportClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: FinanceViewModel = viewModel(),
    modifier: Modifier = Modifier,
    scrollState: ScrollState
) {
    val transactions by viewModel.transactions.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isDarkTheme = remember { ThemeManager.isDarkTheme }

    val totalReceivable by viewModel.totalReceivable.collectAsState()
    val totalRepayable by viewModel.totalRepayable.collectAsState()

    // Calculate totals
    val totalIncome = viewModel.calculateTotalIncome(transactions)
    val totalExpense = viewModel.calculateTotalExpense(transactions)
    val balance = totalIncome - totalExpense

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Finance Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                actions = {
                    // Theme toggle with animation
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

                    // Logout button with confirmation
                    IconButton(
                        onClick = { showLogoutDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Animated FAB
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = onAddTransactionClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Balance Overview Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Current Balance Card with click animation
                var balanceCardPressed by remember { mutableStateOf(false) }
                val balanceCardScale by animateFloatAsState(
                    if (balanceCardPressed) 0.95f else 1f,
                    label = "balanceCardScale"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(balanceCardScale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Optional: Add balance card click action
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    balanceCardPressed = true
                                    tryAwaitRelease()
                                    balanceCardPressed = false
                                }
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (balanceCardPressed) 4.dp else 12.dp
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Balance label with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "CURRENT BALANCE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Balance amount
                        Text(
                            text = formatCurrency(balance),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (balance >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status message with emoji
                        Text(
                            text = if (balance >= 0) "You're doing great! 💰" else "Let's save more! 💪",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Income & Expense Cards with enhanced animations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedStatCard(
                        title = "Total Income",
                        value = "+${formatCurrency(totalIncome)}",
                        icon = Icons.Default.TrendingUp,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onIncomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )

                    AnimatedStatCard(
                        title = "Total Expense",
                        value = "-${formatCurrency(totalExpense)}",
                        icon = Icons.Default.TrendingDown,
                        color = MaterialTheme.colorScheme.error,
                        onClick = onExpenseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )
                }

                // Borrow/Lend Section - ALWAYS SHOW THIS
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Borrow & Lend",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if ((totalReceivable ?: 0.0) > 0 || (totalRepayable ?: 0.0) > 0) {
                        Text(
                            text = "Active records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

// Borrow/Lend Cards with animations
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedBorrowLendCard(
                        title = "People Owe You",
                        amount = totalReceivable ?: 0.0,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onReceivablesClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )

                    AnimatedBorrowLendCard(
                        title = "You Owe Others",
                        amount = totalRepayable ?: 0.0,
                        color = MaterialTheme.colorScheme.error,
                        onClick = onRepayablesClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )
                }
            }


            // Expense Breakdown Section - FIXED: Remove duplicate heading and restore legend
            val expenseTransactions = transactions.filter { it.isExpense }
            if (expenseTransactions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Single header for expense breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expense Breakdown",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "Pie Chart",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Use the ExpensePieChart with proper legend
                    ExpensePieChart(
                        transactions = expenseTransactions,
                        onCategoryClick = onCategoryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Wallets Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wallets",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${viewModel.getPaymentMethods().size} wallets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Wallets List
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.getPaymentMethods().forEach { wallet ->
                        WalletCard(
                            wallet = wallet,
                            transactions = transactions,
                            viewModel = viewModel,
                            onWalletClick = onWalletClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGenerateReportClick() }
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Generate Report",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Generate Financial Report",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Get AI-powered insights and recommendations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        if (pressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "cardElevation"
    )

    // Add ripple effect for better visual feedback
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                onClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with background and scale animation
            val iconScale by animateFloatAsState(
                if (pressed) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "iconScale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(iconScale)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Text with fade animation
            val textAlpha by animateFloatAsState(
                if (pressed) 0.7f else 1f,
                animationSpec = tween(durationMillis = 200),
                label = "textAlpha"
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color.copy(alpha = textAlpha),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Animated arrow indicator
            AnimatedVisibility(
                visible = pressed,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Text(
                    text = "Tap to view →",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
@Composable
fun WalletCard(
    wallet: PaymentMethod,
    transactions: List<Transaction>,
    viewModel: FinanceViewModel,
    onWalletClick: (PaymentMethod) -> Unit
) {
    val income = viewModel.calculateIncomeByPaymentMethod(transactions, wallet)
    val expense = viewModel.calculateExpenseByPaymentMethod(transactions, wallet)
    val balance = viewModel.calculateBalanceByPaymentMethod(transactions, wallet)
    val transactionCount = transactions.count { it.paymentMethod == wallet }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWalletClick(wallet) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Wallet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wallet icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                when (wallet) {
                                    PaymentMethod.CASH -> MaterialTheme.colorScheme.primaryContainer
                                    PaymentMethod.ALIPAY -> MaterialTheme.colorScheme.secondaryContainer
                                    PaymentMethod.OCTOPUS -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Wallet,
                            contentDescription = null,
                            tint = when (wallet) {
                                PaymentMethod.CASH -> MaterialTheme.colorScheme.primary
                                PaymentMethod.ALIPAY -> MaterialTheme.colorScheme.secondary
                                PaymentMethod.OCTOPUS -> MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = when (wallet) {
                                PaymentMethod.CASH -> "Cash Wallet"
                                PaymentMethod.ALIPAY -> "Alipay"
                                PaymentMethod.OCTOPUS -> "Octopus Card"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$transactionCount transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Wallet Balance
                Text(
                    text = formatCurrency(balance),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income vs Expense Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${formatCurrency(income)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-${formatCurrency(expense)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = java.util.Currency.getInstance("HKD")
    return format.format(amount)
}

@Composable
fun AnimatedBorrowLendCard(
    title: String,
    amount: Double,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "borrowLendScale"
    )
    val elevation by animateDpAsState(
        if (pressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "borrowLendElevation"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) {
                onClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with bounce animation
            val iconScale by animateFloatAsState(
                if (pressed) 1.2f else 1f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
                label = "personIconScale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(iconScale)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Animated manage text
            AnimatedVisibility(
                visible = pressed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = "Tap to manage →",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}