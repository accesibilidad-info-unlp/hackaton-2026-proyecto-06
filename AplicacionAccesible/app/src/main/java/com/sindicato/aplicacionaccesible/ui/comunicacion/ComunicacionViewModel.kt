package com.sindicato.aplicacionaccesible.ui.comunicacion

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sindicato.aplicacionaccesible.data.AppDatabase
import com.sindicato.aplicacionaccesible.data.PhraseEntity
import com.sindicato.aplicacionaccesible.ui.theme.AppTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ComunicacionViewModel(application: Application) : AndroidViewModel(application), RecognitionListener {

    private val phraseDao = AppDatabase.getDatabase(application).phraseDao()
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(ComunicacionUiState(
        theme = AppTheme.valueOf(prefs.getString("app_theme", AppTheme.LIGHT.name) ?: AppTheme.LIGHT.name),
        selectedLanguage = prefs.getString("tts_language", "es") ?: "es"
    ))
    val uiState: StateFlow<ComunicacionUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        setupTts()
        setupSpeechRecognizer()
        observePhrases()
    }

    private fun observePhrases() {
        viewModelScope.launch {
            phraseDao.getAllPhrases().collect { phrases ->
                _uiState.update { it.copy(savedPhrases = phrases) }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString("app_theme", theme.name).apply()
        _uiState.update { it.copy(theme = theme) }
    }

    fun setLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        tts?.language = locale
        prefs.edit().putString("tts_language", languageCode).apply()
        
        // Refetch voices for new language
        val voices = try {
            tts?.voices?.filter { 
                it.locale.language == locale.language 
            }?.sortedBy { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        _uiState.update { it.copy(
            selectedLanguage = languageCode,
            availableVoices = voices,
            selectedVoice = tts?.voice
        ) }
    }

    fun saveCurrentTtsAsPhrase() {
        val text = _uiState.value.ttsText
        if (text.isNotBlank()) {
            viewModelScope.launch {
                phraseDao.insertPhrase(PhraseEntity(text = text))
            }
        }
    }

    fun deletePhrase(phrase: PhraseEntity) {
        viewModelScope.launch {
            phraseDao.deletePhrase(phrase)
        }
    }

    private fun setupTts() {
        _uiState.update { it.copy(ttsStatus = TtsStatus.CARGANDO) }
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageCode = prefs.getString("tts_language", "es") ?: "es"
                val currentLocale = Locale(languageCode)
                tts?.language = currentLocale
                
                // Load saved settings
                val savedPitch = prefs.getFloat("tts_pitch", 1.0f)
                val savedRate = prefs.getFloat("tts_rate", 1.0f)
                val savedVoiceName = prefs.getString("tts_voice_name", null)

                tts?.setPitch(savedPitch)
                tts?.setSpeechRate(savedRate)

                // Fetch available voices for current language and sort them
                val voices = try {
                    tts?.voices?.filter { 
                        it.locale.language == currentLocale.language 
                    }?.sortedBy { it.name } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                
                var currentVoice = tts?.voice
                if (savedVoiceName != null) {
                    val matchingVoice = voices.find { it.name == savedVoiceName }
                    if (matchingVoice != null) {
                        tts?.voice = matchingVoice
                        currentVoice = matchingVoice
                    }
                }
                
                _uiState.update { it.copy(
                    ttsStatus = TtsStatus.IDLE,
                    availableVoices = voices,
                    selectedVoice = currentVoice,
                    pitch = savedPitch,
                    speechRate = savedRate,
                    selectedLanguage = languageCode
                ) }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _uiState.update { it.copy(ttsStatus = TtsStatus.REPRODUCIENDO) }
                    }

                    override fun onDone(utteranceId: String?) {
                        _uiState.update { it.copy(ttsStatus = TtsStatus.IDLE) }
                    }

                    @Suppress("DEPRECATION")
                    override fun onError(utteranceId: String?) {
                        _uiState.update { it.copy(ttsStatus = TtsStatus.ERROR, errorMessage = "Error en TTS") }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _uiState.update { it.copy(ttsStatus = TtsStatus.ERROR, errorMessage = "Error en TTS: $errorCode") }
                    }
                })
            } else {
                _uiState.update { it.copy(ttsStatus = TtsStatus.ERROR, errorMessage = "No se pudo inicializar TTS") }
            }
        }
    }

    fun selectVoice(voice: Voice) {
        tts?.voice = voice
        // On some engines, changing voice resets pitch/rate
        tts?.setPitch(_uiState.value.pitch)
        tts?.setSpeechRate(_uiState.value.speechRate)
        
        prefs.edit().putString("tts_voice_name", voice.name).apply()
        _uiState.update { it.copy(selectedVoice = voice) }
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        prefs.edit().putFloat("tts_pitch", pitch).apply()
        _uiState.update { it.copy(pitch = pitch) }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
        prefs.edit().putFloat("tts_rate", rate).apply()
        _uiState.update { it.copy(speechRate = rate) }
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
            speechRecognizer?.setRecognitionListener(this)
        } else {
            _uiState.update { it.copy(errorMessage = "Reconocimiento de voz no disponible") }
        }
    }

    fun changeMode(mode: ComunicacionMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onTtsTextChanged(text: String) {
        _uiState.update { it.copy(ttsText = text) }
    }

    fun onSttTextChanged(text: String) {
        _uiState.update { it.copy(sttText = text) }
    }

    fun speak(textToSpeak: String? = null) {
        val text = textToSpeak ?: _uiState.value.ttsText
        if (text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale(_uiState.value.selectedLanguage))
        }
        speechRecognizer?.startListening(intent)
        _uiState.update { it.copy(sttStatus = SttStatus.ESCUCHANDO) }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {
        _uiState.update { it.copy(sttStatus = SttStatus.ESCUCHANDO) }
    }
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        _uiState.update { it.copy(sttStatus = SttStatus.PROCESANDO) }
    }
    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
            SpeechRecognizer.ERROR_NETWORK -> "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado"
            SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor está ocupado"
            SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
            else -> "Error desconocido"
        }
        _uiState.update { it.copy(sttStatus = SttStatus.ERROR, errorMessage = message) }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _uiState.update { it.copy(sttText = matches[0], sttStatus = SttStatus.IDLE) }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onCleared()
    }
}
