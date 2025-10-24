package com.example.financemanager.model

import java.util.*

data class FinancialReport(
    val period: ReportPeriod,
    val summary: FinancialSummary,
    val transactions: List<Transaction>,
    val receivables: List<BorrowLend>,
    val repayables: List<BorrowLend>,
    val insights: List<FinancialInsight>,
    val generatedDate: Date = Date()
)

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val savingsRate: Double,
    val topSpendingCategory: Category?,
    val topSpendingAmount: Double,
    val walletBalances: Map<PaymentMethod, Double>,
    val totalReceivable: Double,
    val totalRepayable: Double,
    val categoryBreakdown: Map<Category, Double>
)

data class FinancialInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val recommendation: String?,
    val severity: InsightSeverity
)

enum class InsightType {
    SPENDING_PATTERN, SAVINGS_RATE, DEBT_MANAGEMENT, INCOME_GROWTH, BUDGET_ALERT
}

enum class InsightSeverity {
    POSITIVE, NEUTRAL, WARNING, CRITICAL
}

enum class ReportPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly")
}