package com.example.financemanager.model

import java.util.Date

data class BorrowLend(
    val id: String = "",
    val userId: String = "",
    val personName: String = "",
    val amount: Double = 0.0,
    val type: BorrowLendType = BorrowLendType.LENT,
    val description: String = "",
    val date: Date = Date(),
    val dueDate: Date? = null,
    val isSettled: Boolean = false,
    val settledDate: Date? = null,
    val createdAt: Date = Date()
) {
    companion object {
        fun fromMap(map: Map<String, Any>): BorrowLend {
            return BorrowLend(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                personName = map["personName"] as? String ?: "",
                amount = (map["amount"] as? Double) ?: 0.0,
                type = BorrowLendType.valueOf(map["type"] as? String ?: "LENT"),
                description = map["description"] as? String ?: "",
                date = Date((map["date"] as? Long) ?: 0),
                dueDate = (map["dueDate"] as? Long)?.let { Date(it) },
                isSettled = map["isSettled"] as? Boolean ?: false,
                settledDate = (map["settledDate"] as? Long)?.let { Date(it) },
                createdAt = Date((map["createdAt"] as? Long) ?: 0)
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "personName" to personName,
            "amount" to amount,
            "type" to type.name,
            "description" to description,
            "date" to date.time,
            "dueDate" to (dueDate?.time ?: ""),
            "isSettled" to isSettled,
            "settledDate" to (settledDate?.time ?: ""),
            "createdAt" to createdAt.time
        )
    }
}

enum class BorrowLendType {
    BORROWED, LENT
}