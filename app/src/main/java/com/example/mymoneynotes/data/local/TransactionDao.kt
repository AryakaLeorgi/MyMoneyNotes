package com.example.mymoneynotes.data.local

import androidx.room.*
import com.example.mymoneynotes.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction): Int

    @Delete
    suspend fun deleteTransaction(transaction: Transaction): Int
}
