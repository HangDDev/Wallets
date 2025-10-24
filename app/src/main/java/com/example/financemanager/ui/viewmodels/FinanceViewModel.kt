package com.example.financemanager.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.repository.FirebaseRepository
import com.example.financemanager.model.BorrowLend
import com.example.financemanager.model.BorrowLendType
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.PaymentMethod
import com.example.financemanager.model.Category
import com.example.financemanager.model.FinancialInsight
import com.example.financemanager.model.FinancialReport
import com.example.financemanager.model.FinancialSummary
import com.example.financemanager.model.InsightSeverity
import com.example.financemanager.model.InsightType
import com.example.financemanager.model.ReportPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()

    // Current user ID
    var currentUserId: String? = null

    // Transactions State
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    // Borrow/Lend State
    private val _borrowLendRecords = MutableStateFlow<List<BorrowLend>>(emptyList())
    val borrowLendRecords = _borrowLendRecords.asStateFlow()

    // Analytics State
    private val _totalReceivable = MutableStateFlow(0.0)
    val totalReceivable = _totalReceivable.asStateFlow()

    private val _totalRepayable = MutableStateFlow(0.0)
    val totalRepayable = _totalRepayable.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>>
        get() = transactions

    val allBorrowLendRecords: StateFlow<List<BorrowLend>>
        get() = borrowLendRecords

    fun setUser(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            loadAllData(userId)
        }
    }

    private suspend fun loadAllData(userId: String) {
        loadTransactions(userId)
        loadBorrowLendRecords(userId)
        loadAnalytics(userId)
    }

    private suspend fun loadTransactions(userId: String) {
        _transactions.value = repository.getTransactions(userId)
    }

    private suspend fun loadBorrowLendRecords(userId: String) {
        _borrowLendRecords.value = repository.getBorrowLendRecords(userId)
    }

    private suspend fun loadAnalytics(userId: String) {
        _totalReceivable.value = repository.getTotalReceivable(userId)
        _totalRepayable.value = repository.getTotalRepayable(userId)
    }

    // Transaction Operations
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                repository.addTransaction(userId, transaction)
                loadTransactions(userId)
                loadAnalytics(userId)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                repository.deleteTransaction(userId, transaction.id)
                loadTransactions(userId)
                loadAnalytics(userId)
            }
        }
    }

    // Borrow/Lend Operations
    fun addBorrowLendRecord(record: BorrowLend) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                repository.addBorrowLend(userId, record)
                loadBorrowLendRecords(userId)
                loadAnalytics(userId)
            }
        }
    }

    fun updateBorrowLendRecord(record: BorrowLend) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                repository.updateBorrowLend(userId, record)
                loadBorrowLendRecords(userId)
                loadAnalytics(userId)
            }
        }
    }

    fun deleteBorrowLendRecord(record: BorrowLend) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                repository.deleteBorrowLend(userId, record.id)
                loadBorrowLendRecords(userId)
                loadAnalytics(userId)
            }
        }
    }

    // Analytics calculations
    fun calculateTotalIncome(transactions: List<Transaction>): Double {
        return transactions.filter { !it.isExpense }.sumOf { it.amount }
    }

    fun calculateTotalExpense(transactions: List<Transaction>): Double {
        return transactions.filter { it.isExpense }.sumOf { it.amount }
    }

    fun calculateIncomeByPaymentMethod(transactions: List<Transaction>, paymentMethod: PaymentMethod): Double {
        return transactions
            .filter { !it.isExpense && it.paymentMethod == paymentMethod }
            .sumOf { it.amount }
    }

    fun calculateExpenseByPaymentMethod(transactions: List<Transaction>, paymentMethod: PaymentMethod): Double {
        return transactions
            .filter { it.isExpense && it.paymentMethod == paymentMethod }
            .sumOf { it.amount }
    }

    fun calculateBalanceByPaymentMethod(transactions: List<Transaction>, paymentMethod: PaymentMethod): Double {
        val income = calculateIncomeByPaymentMethod(transactions, paymentMethod)
        val expense = calculateExpenseByPaymentMethod(transactions, paymentMethod)
        return income - expense
    }

    fun getPaymentMethods(): List<PaymentMethod> {
        return PaymentMethod.values().toList()
    }

    fun getCategoryTotals(transactions: List<Transaction>): Map<Category, Double> {
        return transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, trans) -> trans.sumOf { it.amount } }
    }

    fun generateFinancialReport(period: ReportPeriod): FinancialReport {
        val currentTransactions = transactions.value
        val currentBorrowLend = borrowLendRecords.value

        val totalIncome = calculateTotalIncome(currentTransactions)
        val totalExpense = calculateTotalExpense(currentTransactions)
        val netBalance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) (netBalance / totalIncome) * 100 else 0.0

        val categoryBreakdown = getCategoryTotals(currentTransactions)
        val topSpendingCategory = categoryBreakdown.maxByOrNull { it.value }?.key
        val topSpendingAmount = topSpendingCategory?.let { categoryBreakdown[it] } ?: 0.0

        val walletBalances = PaymentMethod.values().associateWith { method ->
            calculateBalanceByPaymentMethod(currentTransactions, method)
        }

        val receivables = currentBorrowLend.filter { it.type == BorrowLendType.LENT && !it.isSettled }
        val repayables = currentBorrowLend.filter { it.type == BorrowLendType.BORROWED && !it.isSettled }

        val summary = FinancialSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            savingsRate = savingsRate,
            topSpendingCategory = topSpendingCategory,
            topSpendingAmount = topSpendingAmount,
            walletBalances = walletBalances,
            totalReceivable = totalReceivable.value,
            totalRepayable = totalRepayable.value,
            categoryBreakdown = categoryBreakdown
        )

        // Generate insights (you can enhance this logic)
        val insights = generateFinancialInsights(summary, currentTransactions)

        return FinancialReport(
            period = period,
            summary = summary,
            transactions = currentTransactions,
            receivables = receivables,
            repayables = repayables,
            insights = insights
        )
    }

    fun clearUserData() {
        _transactions.value = emptyList()
        _borrowLendRecords.value = emptyList()
    }

    private fun generateFinancialInsights(summary: FinancialSummary, transactions: List<Transaction>): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()

        // Savings rate insight
        when {
            summary.savingsRate > 30 -> insights.add(
                FinancialInsight(
                    type = InsightType.SAVINGS_RATE,
                    title = "Excellent Savings!",
                    message = "Your savings rate is ${"%.1f".format(summary.savingsRate)}%",
                    recommendation = "Consider investing your surplus funds",
                    severity = InsightSeverity.POSITIVE
                )
            )
            summary.savingsRate < 0 -> insights.add(
                FinancialInsight(
                    type = InsightType.BUDGET_ALERT,
                    title = "Spending Exceeds Income",
                    message = "You're spending more than you earn",
                    recommendation = "Review your expenses and create a budget",
                    severity = InsightSeverity.CRITICAL
                )
            )
        }

        // High spending insight
        if (summary.topSpendingAmount > summary.totalIncome * 0.4) {
            insights.add(
                FinancialInsight(
                    type = InsightType.SPENDING_PATTERN,
                    title = "High Category Spending",
                    message = "${summary.topSpendingCategory?.name ?: "One category"} takes ${"%.0f".format(summary.topSpendingAmount / summary.totalIncome * 100)}% of income",
                    recommendation = "Consider diversifying your spending",
                    severity = InsightSeverity.WARNING
                )
            )
        }

        // Debt management insights
        if (summary.totalRepayable > summary.totalIncome) {
            insights.add(
                FinancialInsight(
                    type = InsightType.DEBT_MANAGEMENT,
                    title = "High Debt Level",
                    message = "Your debt exceeds your monthly income",
                    recommendation = "Focus on debt repayment strategy",
                    severity = InsightSeverity.CRITICAL
                )
            )
        }

        return insights
    }
}