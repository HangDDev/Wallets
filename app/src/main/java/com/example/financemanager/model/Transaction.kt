package com.example.financemanager.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val category: Category = Category.OTHERS,
    val description: String = "",
    val date: Date = Date(),
    val isExpense: Boolean = true,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val createdAt: Date = Date()
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Transaction {
            return Transaction(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                amount = (map["amount"] as? Double) ?: 0.0,
                category = Category.valueOf(map["category"] as? String ?: "OTHERS"),
                description = map["description"] as? String ?: "",
                date = Date((map["date"] as? Long) ?: 0),
                isExpense = map["isExpense"] as? Boolean ?: true,
                paymentMethod = PaymentMethod.valueOf(map["paymentMethod"] as? String ?: "CASH"),
                createdAt = Date((map["createdAt"] as? Long) ?: 0)
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "amount" to amount,
            "category" to category.name,
            "description" to description,
            "date" to date.time,
            "isExpense" to isExpense,
            "paymentMethod" to paymentMethod.name,
            "createdAt" to createdAt.time
        )
    }
}

enum class PaymentMethod {
    CASH, ALIPAY, OCTOPUS
}