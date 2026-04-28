package com.example.mymoneynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.data.model.Category
import com.example.mymoneynotes.data.model.PREDEFINED_CATEGORIES
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    transaction: Transaction?,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, String, String, String, Double, Long) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var selectedDate by remember { mutableLongStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var selectedCat by remember {
        mutableStateOf(
            transaction?.let { Category(it.category, it.emoji, it.type) }
                ?: PREDEFINED_CATEGORIES.first { it.type == TransactionType.EXPENSE }
        )
    }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var amountText by remember { mutableStateOf(if (transaction != null) FormatUtils.formatAmountDots(transaction.amount.toLong().toString()) else "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val availableCats = PREDEFINED_CATEGORIES.filter { it.type == type }
    if (selectedCat.type != type) {
        selectedCat = availableCats.first()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (transaction == null) "Tambah Transaksi" else "Edit Transaksi", color = Color.White)
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Tipe Transaksi", color = Color.White, style = MaterialTheme.typography.titleMedium)
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

                Text("Tanggal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(FormatUtils.formatDate(selectedDate), color = Color.White)
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

                Text("Kategori", color = Color.White, style = MaterialTheme.typography.titleMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(180.dp)
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

                Text("Deskripsi (Opsional)", color = Color.White, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = description,
                    onValueChange = { raw ->
                        if (raw.length <= 100) {
                            description = raw
                        } else {
                            description = raw.take(100)
                        }
                    },
                    label = { Text("Catatan...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    supportingText = {
                        Text("${description.length}/100 karakter", color = Color.Gray)
                    }
                )

                Text("Nominal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val digitsOnly = raw.filter { it.isDigit() }
                        amountText = FormatUtils.formatAmountDots(digitsOnly)
                    },
                    label = { Text("Rp") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawAmount = amountText.replace(".", "")
                    val amount = rawAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(type, selectedCat.name, selectedCat.emoji, description, amount, selectedDate)
                    }
                }
            ) { Text(if (transaction == null) "Simpan" else "Perbarui") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
