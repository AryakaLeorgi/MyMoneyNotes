package com.example.mymoneynotes.data.model

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val category: String,
    val emoji: String,
    val description: String = "",
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)
