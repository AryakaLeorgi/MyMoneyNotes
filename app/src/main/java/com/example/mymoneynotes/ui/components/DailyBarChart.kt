package com.example.mymoneynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyBarChart(transactions: List<Transaction>) {
    // Group by day string like "dd MMM"
    val dailyData = transactions.groupBy { FormatUtils.formatDateShort(it.date) }
    
    // Sort keys by actual date
    val sortedDates = dailyData.keys.sortedBy { key ->
        try {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
            val calendar = Calendar.getInstance()
            val parsed = formatter.parse("$key ${calendar.get(Calendar.YEAR)}")
            parsed?.time ?: 0L
        } catch (e: Exception) { 0L }
    }

    if (sortedDates.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Text("Tidak ada data untuk grafik bar", color = Color.Gray)
        }
        return
    }

    val maxVal = dailyData.values.maxOfOrNull { list ->
        val inc = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val exp = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        maxOf(inc, exp)
    } ?: 1.0

    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        sortedDates.forEach { dateKey ->
            val list = dailyData[dateKey] ?: emptyList()
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.height(150.dp).width(24.dp), contentAlignment = Alignment.BottomCenter) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                        // Income Bar
                        if (income > 0) {
                            val incHeight = (income / maxVal * 150).coerceAtLeast(1.0).dp
                            Box(modifier = Modifier.height(incHeight).width(8.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(IncomeGreen))
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // Expense Bar
                        if (expense > 0) {
                            val expHeight = (expense / maxVal * 150).coerceAtLeast(1.0).dp
                            Box(modifier = Modifier.height(expHeight).width(8.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(ExpenseRed))
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(dateKey.substringBefore(" "), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
