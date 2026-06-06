package com.sindicato.aplicacionaccesible.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String
)
