package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: User)

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET credits = credits + :delta WHERE id = :userId")
    suspend fun addCredits(userId: String, delta: Int)

    @Query("UPDATE users SET totalGenerations = totalGenerations + 1 WHERE id = :userId")
    suspend fun incrementGenerations(userId: String)
}
