package com.sindicato.aplicacionaccesible.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

object SoundEffectManager {
    private var soundPool: SoundPool? = null
    // Map to store Resource ID -> Loaded Sound ID
    private val soundMap = mutableMapOf<Int, Int>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Allows 5 sounds to play simultaneously
            .setAudioAttributes(audioAttributes)
            .build()

        isInitialized = true
    }

    /**
     * Preloads a sound from res/raw.
     * Call this when a button is created or a template is loaded.
     */
    fun loadSound(context: Context, resId: Int) {
        if (soundMap.containsKey(resId)) return

        val loadedId = soundPool?.load(context, resId, 1) ?: -1
        if (loadedId != -1) {
            soundMap[resId] = loadedId
        }
    }

    /**
     * Plays the sound associated with the resId.
     */
    fun playSound(resId: Int) {
        val soundId = soundMap[resId]
        if (soundId != null) {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        } else {
            Log.e("SoundEffectManager", "Sound not loaded for resource ID: $resId")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isInitialized = false
    }
}