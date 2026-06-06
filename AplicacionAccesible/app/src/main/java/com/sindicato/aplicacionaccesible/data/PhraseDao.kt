package com.sindicato.aplicacionaccesible.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases ORDER BY id DESC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)

    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)
}
