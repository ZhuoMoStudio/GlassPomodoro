package com.zhuomo.glasspomodoro.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.abs
import kotlin.math.log10

/**
 * 音频振幅检测器
 * 从麦克风实时读取音频数据，计算归一化振幅值
 */
class AudioAmplitudeDetector(private val context: Context) {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
        // 音频灵敏度 - 降低此值可使动效对更小的声音更敏感
        private const val SENSITIVITY = 45.0
        // 平滑因子 - 越大越平滑（0..1）
        private const val SMOOTHING = 0.65f
    }

    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 开始监听麦克风并实时返回振幅值 (0..1)
     */
    fun startListening(): Flow<Float> = flow {
        if (!hasPermission()) {
            emit(0f)
            return@flow
        }

        val bufferSize = maxOf(
            AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT),
            BUFFER_SIZE
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            emit(0f)
            return@flow
        }

        audioRecord?.startRecording()
        isRecording = true

        val buffer = ShortArray(BUFFER_SIZE)
        var smoothedAmplitude = 0f

        while (isRecording) {
            val readResult = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: -1
            if (readResult > 0) {
                // 计算 RMS 振幅
                var sum = 0.0
                for (i in 0 until readResult) {
                    sum += abs(buffer[i].toDouble())
                }
                val rms = sum / readResult

                // 转换为分贝级别再归一化
                val db = if (rms > 0) {
                    (20 * log10(rms / 32767.0) + SENSITIVITY) / SENSITIVITY
                } else {
                    0.0
                }

                val normalized = db.coerceIn(0.0, 1.0).toFloat()

                // 平滑处理
                smoothedAmplitude = smoothedAmplitude * SMOOTHING + normalized * (1 - SMOOTHING)
                emit(smoothedAmplitude)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun stop() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }
}
