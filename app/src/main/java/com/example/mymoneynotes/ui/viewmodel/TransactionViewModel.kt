package com.example.mymoneynotes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneynotes.MyMoneyApplication
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository = (application as MyMoneyApplication).repository

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(type: TransactionType, category: String, emoji: String, description: String, amount: Double, date: Long) {
        repository.insert(Transaction(type = type, category = category, emoji = emoji, description = description, amount = amount, date = date))
    }

    fun updateTransaction(id: String, type: TransactionType, category: String, emoji: String, description: String, amount: Double, date: Long) {
        repository.update(Transaction(id = id, type = type, category = category, emoji = emoji, description = description, amount = amount, date = date))
    }

    fun deleteTransaction(transaction: Transaction) {
        repository.delete(transaction)
    }
}
