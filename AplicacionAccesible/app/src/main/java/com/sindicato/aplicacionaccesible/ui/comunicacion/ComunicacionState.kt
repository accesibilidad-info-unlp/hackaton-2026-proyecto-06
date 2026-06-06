package com.sindicato.aplicacionaccesible.ui.comunicacion

import android.speech.tts.Voice
import com.sindicato.aplicacionaccesible.data.PhraseEntity
import com.sindicato.aplicacionaccesible.ui.theme.AppTheme

enum class ComunicacionMode {
    TEXT_TO_SPEECH, SPEECH_TO_TEXT
}

enum class TtsStatus {
    IDLE, REPRODUCIENDO, CARGANDO, ERROR
}

enum class SttStatus {
    IDLE, ESCUCHANDO, PROCESANDO, ERROR
}

data class ComunicacionUiState(
    val mode: ComunicacionMode = ComunicacionMode.TEXT_TO_SPEECH,
    val ttsText: String = "",
    val ttsStatus: TtsStatus = TtsStatus.IDLE,
    val sttText: String = "",
    val sttStatus: SttStatus = SttStatus.IDLE,
    val errorMessage: String? = null,
    val savedPhrases: List<PhraseEntity> = emptyList(),
    val availableVoices: List<Voice> = emptyList(),
    val selectedVoice: Voice? = null,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val theme: AppTheme = AppTheme.LIGHT,
    val selectedLanguage: String = "es" // "es" or "en"
)
