package com.example.mymoneynotes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.ui.components.AddEditTransactionDialog
import com.example.mymoneynotes.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMoneyApp(viewModel: TransactionViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyMoney Notes", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("📊", fontSize = 20.sp) },
                    label = { Text("Statistik") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("📋", fontSize = 20.sp) },
                    label = { Text("Riwayat") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingTransaction = null
                    showDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> BerandaScreen(
                    transactions = transactions,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    onNavigate = { selectedTab = it }
                )
                1 -> StatistikScreen(transactions)
                2 -> RiwayatScreen(
                    transactions = transactions,
                    onEdit = { 
                        editingTransaction = it
                        showDialog = true 
                    },
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
        }

        if (showDialog) {
            AddEditTransactionDialog(
                transaction = editingTransaction,
                onDismiss = { showDialog = false },
                onConfirm = { type, catName, emoji, desc, amount, date ->
                    if (editingTransaction == null) {
                        viewModel.addTransaction(type, catName, emoji, desc, amount, date)
                    } else {
                        viewModel.updateTransaction(editingTransaction!!.id, type, catName, emoji, desc, amount, date)
                    }
                    showDialog = false
                }
            )
        }
    }
}
