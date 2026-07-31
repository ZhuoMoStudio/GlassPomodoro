package com.zhuomo.glasspomodoro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 内置白噪音/背景音乐播放器
 *
 * v2.0.2: 暴露 MediaPlayer 的 AudioSessionId，
 * 供 AudioSpectrumAnalyzer 将 Visualizer 附着到内部音频会话，
 * 实现"应用内频响随声音振动"（无需麦克风，100% 可靠）。
 *
 * 音频来源：全部为开源免费音源（CC0 公共领域），详见 LICENSE。
 */
class WhiteNoisePlayer(private val context: Context) {
    private val players = mutableMapOf<String, MediaPlayer>()

    companion object {
        val TRACK_FILES = mapOf(
            "rain" to "rain.wav", "ocean" to "ocean.wav", "fire" to "fire.wav",
            "forest" to "forest.mp3", "stream" to "stream.wav", "wind" to "wind.wav",
            "whitenoise" to "whitenoise.wav", "breath" to "breath.wav"
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

    /** 当前正在播放的音轨对应的音频会话 ID 列表（供 Visualizer 附着） */
    fun getActiveSessionIds(): List<Int> =
        players.values.filter { it.isPlaying }.map { it.audioSessionId }

    /** 最近一个活跃的音频会话 ID；无播放时返回 null */
    fun getLastActiveSessionId(): Int? =
        players.values.firstOrNull { it.isPlaying }?.audioSessionId
}
