package com.example.mymoneynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.mymoneynotes.service.ReminderScheduler
import com.example.mymoneynotes.ui.screen.MyMoneyApp
import com.example.mymoneynotes.ui.theme.MyMoneyNotesTheme
import com.example.mymoneynotes.ui.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule daily reminder
        ReminderScheduler.scheduleDailyReminder(this)
        
        setContent {
            MyMoneyNotesTheme {
                MyMoneyApp(viewModel)
            }
        }
    }
}