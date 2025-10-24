package com.example.financemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemanager.model.BorrowLend
import com.example.financemanager.model.BorrowLendType
import com.example.financemanager.model.PaymentMethod
import com.example.financemanager.model.Transaction
import com.example.financemanager.ui.components.BottomNavigationBar
import com.example.financemanager.ui.screen.AddBorrowLendScreen
import com.example.financemanager.ui.screen.AddTransactionScreen
import com.example.financemanager.ui.screen.CategoryTransactionsScreen
import com.example.financemanager.ui.screen.DashboardScreen
import com.example.financemanager.ui.screen.FilteredTransactionsScreen
import com.example.financemanager.ui.screen.ReceivablesScreen
import com.example.financemanager.ui.screen.RepayablesScreen
import com.example.financemanager.ui.screen.TransactionsScreen
import com.example.financemanager.ui.screen.WalletTransactionsScreen
import com.example.financemanager.ui.viewmodels.FinanceViewModel
import com.example.financemanager.ui.viewmodels.FinanceViewModelFactory
import com.example.financemanager.ui.screen.ReportScreen
import com.example.financemanager.ui.screen.LoginScreen
import com.example.financemanager.ui.screen.SignUpScreen
import com.example.financemanager.service.AuthService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    FinanceAppWithAuth()
                }
            }
        }
    }
}

// Navigation state class
class NavigationState {
    var currentScreen by mutableStateOf("login") // Start with login
    var transactionToEdit by mutableStateOf<Transaction?>(null)
    var selectedWallet by mutableStateOf<PaymentMethod?>(null)
    var selectedCategory by mutableStateOf<String?>(null)
    var selectedTransactionType by mutableStateOf<String?>(null)
    var borrowLendRecordToEdit by mutableStateOf<BorrowLend?>(null)
    var borrowLendScreenType by mutableStateOf<BorrowLendType?>(null)
    var showReportScreen by mutableStateOf(false)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceAppWithAuth() {
    val navigationState = remember { NavigationState() }
    val viewModel: FinanceViewModel = viewModel(
        factory = FinanceViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )

    val authService = remember { AuthService() }
    var isLoggedIn by remember { mutableStateOf(false) }

    val onLogout = {
        authService.signOut()
        isLoggedIn = false
        navigationState.currentScreen = "login"
        // Optionally clear viewModel data
        viewModel.clearUserData()
    }

    // Check authentication state
    LaunchedEffect(Unit) {
        isLoggedIn = authService.isUserLoggedIn()
        if (isLoggedIn) {
            val userId = authService.getCurrentUser()
            if (userId != null) {
                viewModel.setUser(userId)
                navigationState.currentScreen = "dashboard"
            }
        }
    }

    // Handle authentication flows
    if (!isLoggedIn) {
        when (navigationState.currentScreen) {
            "login" -> LoginScreen(
                onLoginSuccess = { userId ->
                    viewModel.setUser(userId)
                    isLoggedIn = true
                    navigationState.currentScreen = "dashboard"
                },
                onSwitchToSignUp = {
                    navigationState.currentScreen = "signup"
                }
            )
            "signup" -> SignUpScreen(
                onSignUpSuccess = { userId ->
                    viewModel.setUser(userId)
                    isLoggedIn = true
                    navigationState.currentScreen = "dashboard"
                },
                onSwitchToLogin = {
                    navigationState.currentScreen = "login"
                }
            )
        }
        return
    }

    // Main app after authentication
    FinanceApp(navigationState, viewModel, authService, onLogout)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceApp(
    navigationState: NavigationState,
    viewModel: FinanceViewModel,
    authService: AuthService,
    onLogout: () -> Unit
) {
    val dashboardScrollState = rememberScrollState()
    val transactionsScrollState = rememberLazyListState()
    val receivablesScrollState = rememberLazyListState()
    val repayablesScrollState = rememberLazyListState()
    val walletTransactionsScrollState = rememberLazyListState()
    val categoryTransactionsScrollState = rememberLazyListState()
    val filteredTransactionsScrollState = rememberLazyListState()

    // Handle system back button
    BackHandler(enabled = navigationState.currentScreen != "dashboard") {
        when (navigationState.currentScreen) {
            "add_transaction", "wallet_transactions", "filtered_transactions",
            "category_transactions", "receivables", "repayables", "add_borrow_lend" -> {
                navigationState.currentScreen = "dashboard"
            }
            "transactions" -> {
                navigationState.currentScreen = "dashboard"
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(navigationState.currentScreen) {
        if (navigationState.currentScreen != "add_transaction" &&
            navigationState.currentScreen != "add_borrow_lend") {
            keyboardController?.hide()
        }
    }

    // Use AnimatedContent for smooth transitions between all screens
    AnimatedContent(
        targetState = navigationState.currentScreen,
        transitionSpec = {
            when {
                initialState == "dashboard" && targetState != "dashboard" && targetState != "transactions" -> {
                    slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) + fadeIn(animationSpec = tween(400)) with
                            slideOutHorizontally(
                                animationSpec = tween(400),
                                targetOffsetX = { fullWidth -> -fullWidth / 4 }
                            ) + fadeOut(animationSpec = tween(400))
                }
                targetState == "dashboard" && initialState != "dashboard" && initialState != "transactions" -> {
                    slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth -> -fullWidth / 4 }
                    ) + fadeIn(animationSpec = tween(400)) with
                            slideOutHorizontally(
                                animationSpec = tween(400),
                                targetOffsetX = { fullWidth -> fullWidth }
                            ) + fadeOut(animationSpec = tween(400))
                }
                (initialState == "dashboard" && targetState == "transactions") ||
                        (initialState == "transactions" && targetState == "dashboard") -> {
                    fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                }
                else -> {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                }
            }
        },
        label = "screen_transitions"
    ) { targetScreen ->
        when (targetScreen) {
            "dashboard" -> {
                androidx.compose.material3.Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            currentScreen = navigationState.currentScreen,
                            onNavigationSelected = { screen ->
                                navigationState.currentScreen = screen
                            }
                        )
                    }
                ) { innerPadding ->
                    DashboardScreen(
                        onAddTransactionClick = {
                            navigationState.transactionToEdit = null
                            navigationState.currentScreen = "add_transaction"
                        },
                        onWalletClick = { wallet: PaymentMethod ->
                            navigationState.selectedWallet = wallet
                            navigationState.currentScreen = "wallet_transactions"
                        },
                        onIncomeClick = {
                            navigationState.selectedTransactionType = "income"
                            navigationState.currentScreen = "filtered_transactions"
                        },
                        onExpenseClick = {
                            navigationState.selectedTransactionType = "expense"
                            navigationState.currentScreen = "filtered_transactions"
                        },
                        onCategoryClick = { category: String ->
                            navigationState.selectedCategory = category
                            navigationState.currentScreen = "category_transactions"
                        },
                        onReceivablesClick = {
                            navigationState.currentScreen = "receivables"
                        },
                        onRepayablesClick = {
                            navigationState.currentScreen = "repayables"
                        },
                        onGenerateReportClick = {
                            navigationState.showReportScreen = true
                        },
                        onLogout = onLogout,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        scrollState = dashboardScrollState,
                    )
                }
            }

            "transactions" -> {
                androidx.compose.material3.Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            currentScreen = navigationState.currentScreen,
                            onNavigationSelected = { screen ->
                                navigationState.currentScreen = screen
                            }
                        )
                    }
                ) { innerPadding ->
                    TransactionsScreen(
                        onEditTransaction = { transaction ->
                            navigationState.transactionToEdit = transaction
                            navigationState.currentScreen = "add_transaction"
                        },
                        viewModel = viewModel,
                        scrollState = transactionsScrollState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            "add_transaction" -> {
                AddTransactionScreen(
                    onBackClick = {
                        navigationState.currentScreen = "dashboard"
                    },
                    transactionToEdit = navigationState.transactionToEdit,
                    viewModel = viewModel
                )
            }

            "wallet_transactions" -> {
                navigationState.selectedWallet?.let { wallet ->
                    WalletTransactionsScreen(
                        wallet = wallet,
                        onBackClick = {
                            navigationState.currentScreen = "dashboard"
                        },
                        onEditTransaction = { transaction ->
                            navigationState.transactionToEdit = transaction
                            navigationState.currentScreen = "add_transaction"
                        },
                        viewModel = viewModel,
                        scrollState = walletTransactionsScrollState
                    )
                }
            }

            "filtered_transactions" -> {
                navigationState.selectedTransactionType?.let { type ->
                    FilteredTransactionsScreen(
                        transactionType = type,
                        onBackClick = {
                            navigationState.currentScreen = "dashboard"
                        },
                        onEditTransaction = { transaction ->
                            navigationState.transactionToEdit = transaction
                            navigationState.currentScreen = "add_transaction"
                        },
                        viewModel = viewModel,
                        scrollState = filteredTransactionsScrollState
                    )
                }
            }

            "category_transactions" -> {
                navigationState.selectedCategory?.let { category ->
                    CategoryTransactionsScreen(
                        category = category,
                        onBackClick = {
                            navigationState.currentScreen = "dashboard"
                        },
                        onEditTransaction = { transaction ->
                            navigationState.transactionToEdit = transaction
                            navigationState.currentScreen = "add_transaction"
                        },
                        viewModel = viewModel,
                        scrollState = categoryTransactionsScrollState
                    )
                }
            }

            "receivables" -> {
                ReceivablesScreen(
                    onBackClick = {
                        navigationState.currentScreen = "dashboard"
                    },
                    onAddRecordClick = {
                        navigationState.borrowLendRecordToEdit = null
                        navigationState.borrowLendScreenType = BorrowLendType.LENT
                        navigationState.currentScreen = "add_borrow_lend"
                    },
                    onEditRecordClick = { record ->
                        navigationState.borrowLendRecordToEdit = record
                        navigationState.borrowLendScreenType = null
                        navigationState.currentScreen = "add_borrow_lend"
                    },
                    viewModel = viewModel,
                    scrollState = receivablesScrollState
                )
            }

            "repayables" -> {
                RepayablesScreen(
                    onBackClick = {
                        navigationState.currentScreen = "dashboard"
                    },
                    onAddRecordClick = {
                        navigationState.borrowLendRecordToEdit = null
                        navigationState.borrowLendScreenType = BorrowLendType.BORROWED
                        navigationState.currentScreen = "add_borrow_lend"
                    },
                    onEditRecordClick = { record ->
                        navigationState.borrowLendRecordToEdit = record
                        navigationState.borrowLendScreenType = null
                        navigationState.currentScreen = "add_borrow_lend"
                    },
                    viewModel = viewModel,
                    scrollState = repayablesScrollState
                )
            }

            "add_borrow_lend" -> {
                AddBorrowLendScreen(
                    onBackClick = {
                        when (navigationState.borrowLendScreenType) {
                            BorrowLendType.LENT -> navigationState.currentScreen = "receivables"
                            BorrowLendType.BORROWED -> navigationState.currentScreen = "repayables"
                            null -> {
                                navigationState.borrowLendRecordToEdit?.let { record ->
                                    navigationState.currentScreen = when (record.type) {
                                        BorrowLendType.LENT -> "receivables"
                                        BorrowLendType.BORROWED -> "repayables"
                                    }
                                } ?: run {
                                    navigationState.currentScreen = "dashboard"
                                }
                            }
                        }
                    },
                    recordToEdit = navigationState.borrowLendRecordToEdit,
                    defaultType = navigationState.borrowLendScreenType,
                    viewModel = viewModel
                )
            }
        }
    }

    if (navigationState.showReportScreen) {
        ReportScreen(
            onBackClick = { navigationState.showReportScreen = false },
            viewModel = viewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FinanceManagerTheme {
        FinanceAppWithAuth()
    }
}