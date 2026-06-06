package com.sindicato.aplicacionaccesible.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.SoundButton
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.SoundEffect
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.Template

import android.speech.tts.TextToSpeech
import java.util.*

class SoundboardViewModel: ViewModel() {

    private val _templates = mutableStateListOf<Template>()
    val templates: List<Template> = _templates
    var currentTemplateIndex by mutableIntStateOf(0)
    var isEditMode by mutableStateOf(false)
    var columnCount by mutableIntStateOf(2)

    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        _templates.add(
            Template(
                "Default", listOf()
            )
        )
    }

    private fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val locale = Locale.getDefault()
                    tts?.language = locale
                    
                    // Load and apply settings from SharedPreferences
                    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    val pitch = prefs.getFloat("tts_pitch", 1.0f)
                    val rate = prefs.getFloat("tts_rate", 1.0f)
                    val voiceName = prefs.getString("tts_voice_name", null)
                    
                    tts?.setPitch(pitch)
                    tts?.setSpeechRate(rate)
                    
                    if (voiceName != null) {
                        val voice = tts?.voices?.find { it.name == voiceName }
                        if (voice != null) {
                            tts?.voice = voice
                        }
                    }
                    
                    isTtsReady = true
                }
            }
        } else {
            // Re-apply settings in case they changed in ConfigurationScreen
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val pitch = prefs.getFloat("tts_pitch", 1.0f)
            val rate = prefs.getFloat("tts_rate", 1.0f)
            val voiceName = prefs.getString("tts_voice_name", null)
            
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
            if (voiceName != null) {
                val voice = tts?.voices?.find { it.name == voiceName }
                if (voice != null) {
                    tts?.voice = voice
                }
            }
        }
    }

    fun addTemplate(name: String) {
        _templates.add(Template(name = name))
        currentTemplateIndex = _templates.size - 1
    }

    fun deleteCurrentTemplate() {
        if (_templates.isNotEmpty()) {
            _templates.removeAt(currentTemplateIndex)
            if (currentTemplateIndex >= _templates.size) {
                currentTemplateIndex = _templates.size - 1
            }
            if (currentTemplateIndex < 0 && _templates.isNotEmpty()) {
                currentTemplateIndex = 0
            }
        }
    }

    fun toggleEditMode() {
        isEditMode = !isEditMode
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addButtonToCurrentTemplate(
        name: String,
        soundEffect: SoundEffect?,
        ttsText: String?,
        colorLong: Long,
        iconRes: Int,
        position: Int
    ) {
        val currentTemplate = _templates.getOrNull(currentTemplateIndex) ?: return
        val updatedButtons = currentTemplate.buttons.filter { it.gridPosition != position } +
                SoundButton(name, position, soundEffect, ttsText, colorLong, iconRes)

        _templates[currentTemplateIndex] = currentTemplate.copy(buttons = updatedButtons)
    }

    fun playSound(context: Context, button: SoundButton) {
        if (button.ttsText != null) {
            initTts(context)
            if (isTtsReady) {
                tts?.speak(button.ttsText, TextToSpeech.QUEUE_FLUSH, null, "soundboard_tts")
            }
        } else if (button.soundEffect != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, button.soundEffect.resourceId)
            mediaPlayer?.start()
        }
    }

    fun deleteButtonAtPosition(position: Int) {
        val currentTemplate = _templates.getOrNull(currentTemplateIndex) ?: return
        val updatedButtons = currentTemplate.buttons.filter { it.gridPosition != position }
        _templates[currentTemplateIndex] = currentTemplate.copy(buttons = updatedButtons)
    }

    fun previewSound(context: Context, soundEffect: SoundEffect) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundEffect.resourceId)
        mediaPlayer?.start()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        tts?.stop()
        tts?.shutdown()
    }
}
