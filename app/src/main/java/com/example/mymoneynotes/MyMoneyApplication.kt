package com.example.mymoneynotes

import android.app.Application
import com.example.mymoneynotes.data.local.AppDatabase
import com.example.mymoneynotes.data.repository.TransactionRepository

class MyMoneyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
