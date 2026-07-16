package com.zhuomo.glasspomodoro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * 白噪音播放器 - 使用 SoundPool 播放内置音效
 * 注意：实际使用时需要将 .ogg/.mp3 文件放入 res/raw/ 目录
 */
class WhiteNoisePlayer(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val loadedIds = mutableMapOf<String, Int>()
    private var isPlaying = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()
    }

    fun loadTrack(trackName: String) {
        if (loadedIds.containsKey(trackName)) return
        val resId = context.resources.getIdentifier(trackName, "raw", context.packageName)
        if (resId != 0) {
            val soundId = soundPool?.load(context, resId, 1) ?: return
            loadedIds[trackName] = soundId
        }
    }

    fun play(trackName: String, loop: Boolean = true) {
        val soundId = loadedIds[trackName] ?: return
        soundPool?.play(soundId, 0.5f, 0.5f, 1, if (loop) -1 else 0, 1f)
        isPlaying = true
    }

    fun stop() {
        soundPool?.autoPause()
        isPlaying = false
    }

    fun release() {
        stop()
        soundPool?.release()
        soundPool = null
        loadedIds.clear()
    }
}
