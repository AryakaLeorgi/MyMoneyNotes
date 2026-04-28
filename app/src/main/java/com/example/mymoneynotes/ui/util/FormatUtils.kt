package com.example.mymoneynotes.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
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
}
