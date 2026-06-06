package com.sindicato.aplicacionaccesible.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sign_language_items")
data class SignLanguageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val imageName: String, // Resource name in drawable
    val audioName: String? = null // Resource name in raw (for future use)
)
