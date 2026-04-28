package com.example.mymoneynotes.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.data.model.PREDEFINED_CATEGORIES
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.components.DailyBarChart
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.util.FormatUtils
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikScreen(transactions: List<Transaction>) {
    // Default range: 1st of current month to today
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.timeInMillis

    var startDate by remember { mutableLongStateOf(startOfMonth) }
    var endDate by remember { mutableLongStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Filter transactions
    val filteredTx = transactions.filter {
        val txCal = Calendar.getInstance().apply { timeInMillis = it.date }
        // normalize to start of day for comparison
        txCal.set(Calendar.HOUR_OF_DAY, 0)
        txCal.set(Calendar.MINUTE, 0)
        txCal.set(Calendar.SECOND, 0)
        txCal.set(Calendar.MILLISECOND, 0)
        val txTime = txCal.timeInMillis
        
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }
        startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
        val endCal = Calendar.getInstance().apply { timeInMillis = endDate }
        endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)

        txTime in startCal.timeInMillis..endCal.timeInMillis
    }

    val totalIncome = filteredTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val total = totalIncome + totalExpense
    val incomePct = if (total > 0) (totalIncome / total * 100).toFloat() else 0f
    val expensePct = if (total > 0) (totalExpense / total * 100).toFloat() else 0f

    val expenses = filteredTx.filter { it.type == TransactionType.EXPENSE }
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries.sortedByDescending { it.value }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Date Range Selector
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Rentang Tanggal", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${FormatUtils.formatDateShort(startDate)} - ${FormatUtils.formatDateShort(endDate)}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text("Grafik Harian", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                DailyBarChart(filteredTx)
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("Arus Kas", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    if (total == 0.0) {
                        Text("Belum ada data", color = Color.Gray)
                    } else {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val strokeWidth = 32.dp.toPx()
                            val incomeAngle = (incomePct / 100f) * 360f
                            val expenseAngle = (expensePct / 100f) * 360f
                            
                            if (expenseAngle > 0) {
                                drawArc(color = ExpenseRed, startAngle = -90f, sweepAngle = expenseAngle, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                            }
                            if (incomeAngle > 0) {
                                drawArc(color = IncomeGreen, startAngle = -90f + expenseAngle, sweepAngle = incomeAngle, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                            Text(FormatUtils.formatCurrency(totalIncome - totalExpense), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pemasukan", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(FormatUtils.formatCurrency(totalIncome), color = IncomeGreen, fontWeight = FontWeight.Bold)
                        Text("${incomePct.roundToInt()}%", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pengeluaran", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(FormatUtils.formatCurrency(totalExpense), color = ExpenseRed, fontWeight = FontWeight.Bold)
                        Text("${expensePct.roundToInt()}%", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Rincian Pengeluaran", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(categoryTotals) { entry ->
                val cat = PREDEFINED_CATEGORIES.find { it.name == entry.key }
                val emoji = cat?.emoji ?: "🔹"
                val pct = if (totalExpense > 0) (entry.value / totalExpense * 100).toFloat() else 0f
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.key, color = Color.White)
                            Text(FormatUtils.formatCurrency(entry.value), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ExpenseRed,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    val s = dateRangePickerState.selectedStartDateMillis
                    val e = dateRangePickerState.selectedEndDateMillis
                    if (s != null && e != null) {
                        val diffDays = (e - s) / (1000 * 60 * 60 * 24)
                        if (diffDays > 31) {
                            // Automatically clamp to 31 days if too large
                            startDate = e - (31L * 24 * 60 * 60 * 1000)
                            endDate = e
                        } else {
                            startDate = s
                            endDate = e
                        }
                    }
                    showDatePicker = false 
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text(text = "Pilih Rentang Tanggal (Maks 31 Hari)", modifier = Modifier.padding(16.dp)) },
                headline = { Text("", modifier = Modifier.height(0.dp)) },
                showModeToggle = false
            )
        }
    }
}
