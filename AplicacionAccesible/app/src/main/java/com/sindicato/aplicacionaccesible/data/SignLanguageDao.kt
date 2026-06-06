package com.sindicato.aplicacionaccesible.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignLanguageDao {
    @Query("SELECT * FROM sign_language_items")
    fun getAllItems(): Flow<List<SignLanguageEntity>>

    @Query("SELECT COUNT(*) FROM sign_language_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SignLanguageEntity>)
}
