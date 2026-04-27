package com.example.mymoneynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

    fun addTransaction(type: TransactionType, category: String, emoji: String, amount: Double) {
        transactions = listOf(Transaction(type = type, category = category, emoji = emoji, amount = amount)) + transactions
    }

    fun updateTransaction(id: String, type: TransactionType, category: String, emoji: String, amount: Double) {
        transactions = transactions.map {
            if (it.id == id) it.copy(type = type, category = category, emoji = emoji, amount = amount) else it
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
                1 -> StatistikTab(viewModel.transactions, viewModel.totalIncome, viewModel.totalExpense)
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
                onConfirm = { type, catName, emoji, amount ->
                    if (editingTransaction == null) {
                        viewModel.addTransaction(type, catName, emoji, amount)
                    } else {
                        viewModel.updateTransaction(editingTransaction!!.id, type, catName, emoji, amount)
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
        
        val recent = viewModel.transactions.take(5)
        if (recent.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi 🍃", color = Color.Gray)
            }
        } else {
            recent.forEach { tx ->
                TransactionRow(tx)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatistikTab(transactions: List<Transaction>, totalIncome: Double, totalExpense: Double) {
    val total = totalIncome + totalExpense
    val incomePct = if (total > 0) (totalIncome / total * 100).toFloat() else 0f
    val expensePct = if (total > 0) (totalExpense / total * 100).toFloat() else 0f

    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries.sortedByDescending { it.value }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Arus Kas", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            if (total == 0.0) {
                Text("Belum ada data 🍩", color = Color.Gray)
            } else {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 32.dp.toPx()
                    val incomeAngle = (incomePct / 100f) * 360f
                    val expenseAngle = (expensePct / 100f) * 360f
                    
                    if (expenseAngle > 0) {
                        drawArc(
                            color = ExpenseRed,
                            startAngle = -90f,
                            sweepAngle = expenseAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    if (incomeAngle > 0) {
                        drawArc(
                            color = IncomeGreen,
                            startAngle = -90f + expenseAngle,
                            sweepAngle = incomeAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(IncomeGreen))
                Spacer(Modifier.width(8.dp))
                Text("Masuk ${incomePct.roundToInt()}%", color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ExpenseRed))
                Spacer(Modifier.width(8.dp))
                Text("Keluar ${expensePct.roundToInt()}%", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Rincian Pengeluaran", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
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
}

@Composable
fun RiwayatTab(transactions: List<Transaction>, onEdit: (Transaction) -> Unit, onDelete: (Transaction) -> Unit) {
    val grouped = transactions.groupBy { formatDate(it.date) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Riwayat Transaksi", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Riwayat kosong \uD83D\uDCEB", color = Color.Gray)
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
                Text(formatDate(tx.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
    onConfirm: (TransactionType, String, String, Double) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedCat by remember { 
        mutableStateOf(
            transaction?.let { Category(it.category, it.emoji, it.type) } 
                ?: PREDEFINED_CATEGORIES.first { it.type == TransactionType.EXPENSE }
        ) 
    }
    var amountText by remember { mutableStateOf(if (transaction != null) transaction.amount.toLong().toString() else "") }
    
    val availableCats = PREDEFINED_CATEGORIES.filter { it.type == type }
    if (selectedCat.type != type) {
        selectedCat = availableCats.first()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) "Tambah Transaksi" else "Edit Transaksi", color = Color.White) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                
                Text("Kategori", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(140.dp)
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
                
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    label = { Text("Nominal") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("Rp ") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(type, selectedCat.name, selectedCat.emoji, amount)
                    }
                }
            ) { Text(if (transaction == null) "Simpan" else "Perbarui") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// --- Utils ---

fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
    return formatter.format(Date(timestamp))
}