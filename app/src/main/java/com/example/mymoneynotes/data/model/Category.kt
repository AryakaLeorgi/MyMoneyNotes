package com.example.mymoneynotes.data.model

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
