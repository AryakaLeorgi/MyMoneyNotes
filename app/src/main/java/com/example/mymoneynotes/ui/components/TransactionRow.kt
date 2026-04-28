package com.example.mymoneynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneynotes.data.model.Transaction
import com.example.mymoneynotes.data.model.TransactionType
import com.example.mymoneynotes.ui.theme.ExpenseRed
import com.example.mymoneynotes.ui.theme.IncomeGreen
import com.example.mymoneynotes.ui.util.FormatUtils

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
                    Text(tx.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                } else {
                    Text(FormatUtils.formatDate(tx.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Text(
                (if (tx.type == TransactionType.INCOME) "+" else "-") + FormatUtils.formatCurrency(tx.amount),
                color = if (tx.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
