package com.example.mymoneynotes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mymoneynotes.data.model.DateFilter
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.components.TransactionRow
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.util.FormatUtils
import java.util.*

@Composable
fun BerandaScreen(
    transactions: List<Transaction>,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    onNavigate: (Int) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(DateFilter.ALL_TIME) }
    var expanded by remember { mutableStateOf(false) }

    val startTime = remember(selectedFilter) {
        val calendar = Calendar.getInstance()
        when (selectedFilter) {
            DateFilter.THIRTY_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.timeInMillis
            }
            DateFilter.HALF_YEAR -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.timeInMillis
            }
            DateFilter.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            DateFilter.ALL_TIME -> 0L
        }
    }

    val filteredTransactions = transactions.filter { it.date >= startTime }
    val displayIncome = if (selectedFilter == DateFilter.ALL_TIME) totalIncome else filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val displayExpense = if (selectedFilter == DateFilter.ALL_TIME) totalExpense else filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val displayBalance = displayIncome - displayExpense

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter: ", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(selectedFilter.label, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DateFilter.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter.label, color = Color.White) },
                            onClick = {
                                selectedFilter = filter
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Saldo", color = Color.LightGray, style = MaterialTheme.typography.titleMedium)
                Text(
                    FormatUtils.formatCurrency(displayBalance),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("▼ Pemasukan", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        Text(FormatUtils.formatCurrency(displayIncome), color = IncomeGreen, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("▲ Pengeluaran", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        Text(FormatUtils.formatCurrency(displayExpense), color = ExpenseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Transaksi Terakhir", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Lihat Semua →", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onNavigate(2) })
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val recent = transactions.sortedByDescending { it.date }.take(5)
        if (recent.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi", color = Color.Gray)
            }
        } else {
            recent.forEach { tx ->
                TransactionRow(tx)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
