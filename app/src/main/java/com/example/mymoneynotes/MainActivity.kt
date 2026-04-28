package com.example.mymoneynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.theme.MyMoneyNotesTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// --- Models & Data ---

enum class TransactionType { INCOME, EXPENSE }

data class Category(val name: String, val emoji: String, val type: TransactionType)

val PREDEFINED_CATEGORIES = listOf(
    Category("Gaji", "💰", TransactionType.INCOME),
    Category("Bonus", "🎁", TransactionType.INCOME),
    Category("Freelance", "💼", TransactionType.INCOME),
    Category("Investasi", "📈", TransactionType.INCOME),
    Category("Makanan", "🍔", TransactionType.EXPENSE),
    Category("Transportasi", "🚗", TransactionType.EXPENSE),
    Category("Tempat Tinggal", "🏠", TransactionType.EXPENSE),
    Category("Belanja", "🛒", TransactionType.EXPENSE),
    Category("Hiburan", "📱", TransactionType.EXPENSE),
    Category("Kesehatan", "💊", TransactionType.EXPENSE),
    Category("Pendidikan", "📚", TransactionType.EXPENSE),
    Category("Lainnya", "🔧", TransactionType.EXPENSE),
    Category("Lainnya", "🏦", TransactionType.INCOME)
)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val category: String,
    val emoji: String,
    val description: String = "",
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

// --- ViewModel ---

class MyMoneyViewModel {
    var transactions by mutableStateOf(listOf<Transaction>())
        private set

    val totalIncome: Double get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense: Double get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance: Double get() = totalIncome - totalExpense

    fun addTransaction(type: TransactionType, category: String, emoji: String, description: String, amount: Double, date: Long) {
        transactions = listOf(Transaction(type = type, category = category, emoji = emoji, description = description, amount = amount, date = date)) + transactions
    }

    fun updateTransaction(id: String, type: TransactionType, category: String, emoji: String, description: String, amount: Double, date: Long) {
        transactions = transactions.map {
            if (it.id == id) it.copy(type = type, category = category, emoji = emoji, description = description, amount = amount, date = date) else it
        }
    }

    fun deleteTransaction(id: String) {
        transactions = transactions.filterNot { it.id == id }
    }
}

// --- Main Activity ---

class MainActivity : ComponentActivity() {
    private val viewModel = MyMoneyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMoneyNotesTheme {
                MyMoneyApp(viewModel)
            }
        }
    }
}

// --- Navigation & Core UI ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMoneyApp(viewModel: MyMoneyViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

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
                0 -> BerandaTab(viewModel) { tabIndex -> selectedTab = tabIndex }
                1 -> StatistikTab(viewModel.transactions)
                2 -> RiwayatTab(
                    transactions = viewModel.transactions,
                    onEdit = { 
                        editingTransaction = it
                        showDialog = true 
                    },
                    onDelete = { viewModel.deleteTransaction(it.id) }
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

// --- Tabs ---

@Composable
fun BerandaTab(viewModel: MyMoneyViewModel, onNavigate: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                    formatCurrency(viewModel.balance),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("▼ Pemasukan", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        Text(formatCurrency(viewModel.totalIncome), color = IncomeGreen, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("▲ Pengeluaran", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        Text(formatCurrency(viewModel.totalExpense), color = ExpenseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Transaksi Terakhir", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Lihat Semua →", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onNavigate(2) })
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val recent = viewModel.transactions.sortedByDescending { it.date }.take(5)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikTab(transactions: List<Transaction>) {
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
                    Text("${formatDateShort(startDate)} - ${formatDateShort(endDate)}", color = Color.White, fontWeight = FontWeight.Bold)
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
                            Text(formatCurrency(totalIncome - totalExpense), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pemasukan", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(formatCurrency(totalIncome), color = IncomeGreen, fontWeight = FontWeight.Bold)
                        Text("${incomePct.roundToInt()}%", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pengeluaran", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Text(formatCurrency(totalExpense), color = ExpenseRed, fontWeight = FontWeight.Bold)
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
                            Text(formatCurrency(entry.value), color = Color.White, fontWeight = FontWeight.Bold)
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

@Composable
fun DailyBarChart(transactions: List<Transaction>) {
    // Group by day string like "dd MMM"
    val dailyData = transactions.groupBy { formatDateShort(it.date) }
    
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

@Composable
fun RiwayatTab(transactions: List<Transaction>, onEdit: (Transaction) -> Unit, onDelete: (Transaction) -> Unit) {
    val grouped = transactions.sortedByDescending { it.date }.groupBy { formatDate(it.date) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Riwayat Transaksi", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Riwayat kosong", color = Color.Gray)
            }
        } else {
            LazyColumn {
                grouped.forEach { (dateStr, txList) ->
                    item {
                        Text(
                            text = dateStr,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(txList) { tx ->
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tx.emoji, fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.category, color = Color.White, fontWeight = FontWeight.Bold)
                                    if (tx.description.isNotBlank()) {
                                        Text(tx.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(
                                        if (tx.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran",
                                        style = MaterialTheme.typography.bodySmall, color = Color.Gray
                                    )
                                }
                                Text(
                                    (if (tx.type == TransactionType.INCOME) "+" else "-") + formatCurrency(tx.amount),
                                    color = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onEdit(tx) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Hapus Transaksi?") },
                                text = { Text("Transaksi ini akan dihapus permanen.") },
                                confirmButton = {
                                    Button(
                                        onClick = { 
                                            onDelete(tx)
                                            showDeleteConfirm = false 
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                                    ) { Text("Hapus") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(tx.emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.category, color = Color.White, fontWeight = FontWeight.Bold)
                if (tx.description.isNotBlank()) {
                    Text(tx.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Text(formatDate(tx.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Text(
                (if (tx.type == TransactionType.INCOME) "+" else "-") + formatCurrency(tx.amount),
                color = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Dialogs ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    transaction: Transaction?,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, String, String, String, Double, Long) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedDate by remember { mutableLongStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var selectedCat by remember { 
        mutableStateOf(
            transaction?.let { Category(it.category, it.emoji, it.type) } 
                ?: PREDEFINED_CATEGORIES.first { it.type == TransactionType.EXPENSE }
        ) 
    }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var amountText by remember { mutableStateOf(if (transaction != null) formatAmountDots(transaction.amount.toLong().toString()) else "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val availableCats = PREDEFINED_CATEGORIES.filter { it.type == type }
    if (selectedCat.type != type) {
        selectedCat = availableCats.first()
    }

    val totalSteps = 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(if (transaction == null) "Tambah Transaksi" else "Edit Transaksi", color = Color.White)
                Spacer(Modifier.height(8.dp))
                // Step indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 0 until totalSteps) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (i == step) MaterialTheme.colorScheme.primary else Color.Gray))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.Center) {
                when (step) {
                    0 -> { // Type
                        Text("Tipe Transaksi", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            FilterChip(
                                selected = type == TransactionType.EXPENSE,
                                onClick = { type = TransactionType.EXPENSE },
                                label = { Text("Pengeluaran") }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = type == TransactionType.INCOME,
                                onClick = { type = TransactionType.INCOME },
                                label = { Text("Pemasukan") }
                            )
                        }
                    }
                    1 -> { // Date
                        Text("Tanggal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(formatDate(selectedDate), color = Color.White)
                        }
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                                        showDatePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }
                    2 -> { // Category
                        Text("Kategori", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(160.dp)
                        ) {
                            items(availableCats) { cat ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedCat.name == cat.name) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                                        .clickable { selectedCat = cat }
                                        .padding(8.dp)
                                ) {
                                    Text(cat.emoji, fontSize = 24.sp)
                                    Text(cat.name, fontSize = 10.sp, color = Color.LightGray, maxLines = 1)
                                }
                            }
                        }
                    }
                    3 -> { // Description
                        Text("Deskripsi (Opsional)", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Catatan...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                    4 -> { // Amount
                        Text("Nominal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { raw -> 
                                val digitsOnly = raw.filter { it.isDigit() }
                                amountText = formatAmountDots(digitsOnly)
                            },
                            label = { Text("Rp") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (step < totalSteps - 1) {
                Button(onClick = { step++ }) { Text("Selanjutnya") }
            } else {
                Button(
                    onClick = {
                        val rawAmount = amountText.replace(".", "")
                        val amount = rawAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onConfirm(type, selectedCat.name, selectedCat.emoji, description, amount, selectedDate)
                        }
                    }
                ) { Text(if (transaction == null) "Simpan" else "Perbarui") }
            }
        },
        dismissButton = {
            if (step > 0) {
                TextButton(onClick = { step-- }) { Text("Kembali") }
            } else {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}

// --- Utils ---

fun formatAmountDots(input: String): String {
    if (input.isEmpty()) return ""
    return try {
        val parsed = input.toLong()
        val format = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        format.format(parsed)
    } catch (e: Exception) {
        input
    }
}

fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
    return formatter.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM", Locale.Builder().setLanguage("id").setRegion("ID").build())
    return formatter.format(Date(timestamp))
}