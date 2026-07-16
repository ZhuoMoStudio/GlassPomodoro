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
import kotlin.math.sqrt

class AudioAmplitudeDetector(private val context: Context) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
        private const val SENSITIVITY = 50.0
        private const val SMOOTHING = 0.6f
    }

    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun startListening(): Flow<Float> = flow {
        if (!hasPermission()) { emit(0f); return@flow }
        val bufferSize = maxOf(AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT), BUFFER_SIZE)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) { emit(0f); return@flow }
        audioRecord?.startRecording()
        isRecording = true
        val buffer = ShortArray(BUFFER_SIZE)
        var smoothed = 0f
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: -1
            if (read > 0) {
                var sumSquares = 0.0
                for (i in 0 until read) sumSquares += buffer[i].toDouble() * buffer[i].toDouble()
                val rms = sqrt(sumSquares / read)
                val db = if (rms > 0) (20 * log10(rms / 32767.0) + SENSITIVITY) / SENSITIVITY else 0.0
                val normalized = db.coerceIn(0.0, 1.0).toFloat()
                smoothed = smoothed * SMOOTHING + normalized * (1 - SMOOTHING)
                emit(smoothed)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun stop() {
        isRecording = false
        try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
        audioRecord = null
    }
}
