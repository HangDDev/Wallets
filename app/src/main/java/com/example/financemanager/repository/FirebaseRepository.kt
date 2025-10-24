package com.example.financemanager.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.example.financemanager.model.Transaction
import com.example.financemanager.model.BorrowLend
import java.util.UUID

class FirebaseRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    // Transactions Collection
    private fun getTransactionsCollection(userId: String) =
        db.collection("users").document(userId).collection("transactions")

    // Borrow/Lend Collection
    private fun getBorrowLendCollection(userId: String) =
        db.collection("users").document(userId).collection("borrow_lend")

    // Transaction Operations
    suspend fun addTransaction(userId: String, transaction: Transaction): String {
        val transactionId = UUID.randomUUID().toString()
        val transactionWithId = transaction.copy(id = transactionId)

        getTransactionsCollection(userId)
            .document(transactionId)
            .set(transactionWithId.toMap())
            .await()

        return transactionId
    }

    suspend fun getTransactions(userId: String): List<Transaction> {
        val snapshot = getTransactionsCollection(userId)
            .orderBy("date")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Transaction.fromMap(doc.data ?: emptyMap())
        }
    }

    suspend fun updateTransaction(userId: String, transaction: Transaction) {
        getTransactionsCollection(userId)
            .document(transaction.id)
            .set(transaction.toMap())
            .await()
    }

    suspend fun deleteTransaction(userId: String, transactionId: String) {
        getTransactionsCollection(userId)
            .document(transactionId)
            .delete()
            .await()
    }

    // Borrow/Lend Operations
    suspend fun addBorrowLend(userId: String, record: BorrowLend): String {
        val recordId = UUID.randomUUID().toString()
        val recordWithId = record.copy(id = recordId)

        getBorrowLendCollection(userId)
            .document(recordId)
            .set(recordWithId.toMap())
            .await()

        return recordId
    }

    suspend fun getBorrowLendRecords(userId: String): List<BorrowLend> {
        val snapshot = getBorrowLendCollection(userId)
            .orderBy("date")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            BorrowLend.fromMap(doc.data ?: emptyMap())
        }
    }

    suspend fun updateBorrowLend(userId: String, record: BorrowLend) {
        getBorrowLendCollection(userId)
            .document(record.id)
            .set(record.toMap())
            .await()
    }

    suspend fun deleteBorrowLend(userId: String, recordId: String) {
        getBorrowLendCollection(userId)
            .document(recordId)
            .delete()
            .await()
    }

    // Analytics Queries
    suspend fun getTotalReceivable(userId: String): Double {
        val snapshot = getBorrowLendCollection(userId)
            .whereEqualTo("type", "LENT")
            .whereEqualTo("isSettled", false)
            .get()
            .await()

        return snapshot.documents.sumOf { doc ->
            (doc.getDouble("amount") ?: 0.0)
        }
    }

    suspend fun getTotalRepayable(userId: String): Double {
        val snapshot = getBorrowLendCollection(userId)
            .whereEqualTo("type", "BORROWED")
            .whereEqualTo("isSettled", false)
            .get()
            .await()

        return snapshot.documents.sumOf { doc ->
            (doc.getDouble("amount") ?: 0.0)
        }
    }
}