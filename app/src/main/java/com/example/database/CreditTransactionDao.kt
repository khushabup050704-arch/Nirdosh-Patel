package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.models.CreditTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditTransactionDao {
    @Query("SELECT * FROM credit_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<CreditTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CreditTransaction)

    @Query("SELECT SUM(amount) FROM credit_transactions WHERE userId = :userId")
    suspend fun getTotalCredits(userId: String): Int?
}
