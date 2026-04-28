package com.example.mymoneynotes

import android.app.Application
import com.example.mymoneynotes.data.repository.TransactionRepository

class MyMoneyApplication : Application() {
    val repository by lazy { TransactionRepository() }
}
