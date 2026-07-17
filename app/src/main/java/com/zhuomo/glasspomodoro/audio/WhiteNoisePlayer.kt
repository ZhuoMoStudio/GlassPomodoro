package com.zhuomo.glasspomodoro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhiteNoisePlayer(private val context: Context) {
    private val players = mutableMapOf<String, MediaPlayer>()

    companion object {
        val TRACK_FILES = mapOf(
            "rain" to "rain.wav", "ocean" to "ocean.wav", "fire" to "fire.wav",
            "forest" to "forest.mp3", "stream" to "stream.wav", "wind" to "wind.wav",
            "whitenoise" to "whitenoise.wav"
        )
    }

    suspend fun play(trackName: String, volume: Float = 0.5f, loop: Boolean = true): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = TRACK_FILES[trackName] ?: "$trackName.wav"
                val resId = context.resources.getIdentifier(fileName.substringBeforeLast("."), "raw", context.packageName)
                if (resId == 0) return@withContext false
                stop(trackName)
                val mp = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/$resId"))
                    prepare(); isLooping = loop; setVolume(volume, volume); start()
                }
                players[trackName] = mp; true
            } catch (_: Exception) { false }
        }
    }

    fun stop(trackName: String) { players[trackName]?.let { try { it.stop(); it.release() } catch(_:Exception){} }; players.remove(trackName) }
    fun stopAll() { players.values.forEach { try { it.stop(); it.release() } catch(_:Exception){} }; players.clear() }
    fun setVolume(trackName: String, volume: Float) { players[trackName]?.setVolume(volume, volume) }
    fun isPlaying(trackName: String): Boolean = players[trackName]?.isPlaying ?: false
}
