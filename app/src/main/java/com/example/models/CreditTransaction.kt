package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "credit_transactions")
data class CreditTransaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "user_default",
    val amount: Int, // e.g. -2 for video generation, +50 for bonus
    val type: String, // "generation", "bonus", "purchase", "admin_grant"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
