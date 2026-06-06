package com.sindicato.aplicacionaccesible.ui.screens.soundgrid

import android.graphics.Color

data class SoundButton(
    val name: String,
    val gridPosition: Int,
    val soundEffect: SoundEffect?,
    val ttsText: String? = null,
    val color: Long,
    val iconRes: Int
)
