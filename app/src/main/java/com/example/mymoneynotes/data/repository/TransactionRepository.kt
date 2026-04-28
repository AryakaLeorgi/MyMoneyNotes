package com.example.mymoneynotes.data.repository

import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow

class TransactionRepository {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: Flow<List<Transaction>> = _transactions.asStateFlow()

    val totalIncome: Flow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    val totalExpense: Flow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    fun getTransactionsSince(startTime: Long): Flow<List<Transaction>> {
        return allTransactions.map { list ->
            list.filter { it.date >= startTime }
        }
    }

    fun insert(transaction: Transaction) {
        _transactions.value = listOf(transaction) + _transactions.value
    }

    fun update(transaction: Transaction) {
        _transactions.value = _transactions.value.map {
            if (it.id == transaction.id) transaction else it
        }
    }

    fun delete(transaction: Transaction) {
        _transactions.value = _transactions.value.filterNot { it.id == transaction.id }
    }
}
