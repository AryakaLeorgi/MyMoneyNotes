package com.example.mymoneynotes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val category: String,
    val emoji: String,
    val description: String = "",
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)
