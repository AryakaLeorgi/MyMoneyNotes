package com.example.mymoneynotes.data.repository

import com.example.mymoneynotes.data.local.TransactionDao
import com.example.mymoneynotes.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    val totalIncome: Flow<Double> = transactionDao.getTotalIncome().map { it ?: 0.0 }
    val totalExpense: Flow<Double> = transactionDao.getTotalExpense().map { it ?: 0.0 }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
}
