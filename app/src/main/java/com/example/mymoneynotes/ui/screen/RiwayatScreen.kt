package com.example.mymoneynotes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.util.FormatUtils

@Composable
fun RiwayatScreen(
    transactions: List<Transaction>,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    val grouped = transactions.sortedByDescending { it.date }.groupBy { FormatUtils.formatDate(it.date) }
    
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
                                    (if (tx.type == TransactionType.INCOME) "+" else "-") + FormatUtils.formatCurrency(tx.amount),
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
