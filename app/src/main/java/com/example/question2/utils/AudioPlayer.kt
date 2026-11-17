package com.example.question2.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(
        context: Context,
        audioUrl: String,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (audioUrl.isBlank()) {
            onError("Audio URL is not available for this word.")
            return
        }

        stopAudio()

        var formattedUrl = audioUrl
        if (formattedUrl.startsWith("//")) {
            formattedUrl = "https:$formattedUrl"
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(formattedUrl)
                setOnPreparedListener { mp ->
                    onStart()
                    mp.start()
                }
                setOnCompletionListener {
                    stopAudio()
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error playing audio: what=$what, extra=$extra")
                    stopAudio()
                    onError("Failed to play pronunciation audio.")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Exception in playAudio", e)
            stopAudio()
            onError("Error initializing audio player: ${e.localizedMessage}")
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Exception stopping audio", e)
        } finally {
            mediaPlayer = null
        }
    }
}