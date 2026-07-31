package com.zhuomo.glasspomodoro.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import com.zhuomo.glasspomodoro.model.SpectrumData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * v2.0: 基于 Visualizer API 的实时频谱分析器
 *
 * 将 FFT 输出按频段映射：
 * - 低频 (20~250Hz)  → 波幅 (A0)
 * - 中频 (250~4kHz)  → 波纹密度/速度 (ω、k)
 * - 高频 (4kHz~16kHz) → 粒子/纹理强度
 *
 * 使用 Visualizer 附加到音频输出会话（sessionId = 0），
 * 可捕获系统所有正在播放的音频（含白噪音与外部音乐）。
 */
class AudioSpectrumAnalyzer(private val context: Context) {

    companion object {
        private const val CAPTURE_SIZE = 1024
        private const val SMOOTHING = 0.55f
    }

    private val _spectrum = MutableStateFlow(SpectrumData.EMPTY)
    val spectrum: StateFlow<SpectrumData> = _spectrum.asStateFlow()

    private var visualizer: Visualizer? = null
    private var isListening = false

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    /**
     * 开始监听并持续输出频段数据流
     */
    fun startListening(): Flow<SpectrumData> = flow {
        if (!hasPermission()) { emit(SpectrumData.EMPTY); return@flow }
        try {
            val capSize = Visualizer.getCaptureSizeRange().let { it[1].coerceAtMost(CAPTURE_SIZE) }
            visualizer = Visualizer(0).apply {
                enabled = false
                captureSize = capSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        // 波形数据（备用于时域分析，此处不消费）
                    }

                    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        fft?.let { _spectrum.value = analyzeFft(it, samplingRate) }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                enabled = true
            }
            isListening = true
            var smoothed = SpectrumData.EMPTY
            while (isListening) {
                val cur = _spectrum.value
                smoothed = SpectrumData(
                    low = smooth(smoothed.low, cur.low),
                    mid = smooth(smoothed.mid, cur.mid),
                    high = smooth(smoothed.high, cur.high),
                    overall = smooth(smoothed.overall, cur.overall)
                )
                emit(smoothed)
                kotlinx.coroutines.delay(32)
            }
        } catch (_: Exception) {
            emit(SpectrumData.EMPTY)
        }
    }.flowOn(Dispatchers.IO)

    private fun smooth(prev: Float, next: Float): Float =
        prev * SMOOTHING + next * (1f - SMOOTHING)

    /**
     * FFT 字节数组 → 三频段能量
     * fft 数据格式: 每对字节为 (实部, 虚部)，从 0Hz 开始
     */
    private fun analyzeFft(fft: ByteArray, samplingRate: Int): SpectrumData {
        val n = fft.size / 2
        if (n < 2) return SpectrumData.EMPTY
        val magnitudes = FloatArray(n) { i ->
            val real = fft[i * 2].toDouble()
            val imag = fft[i * 2 + 1].toDouble()
            val mag = sqrt(real * real + imag * imag)
            if (mag > 0) (20.0 * ln(mag + 1e-6) / 20.0).toFloat() else 0f
        }
        // 归一化：除以最大值
        var maxMag = 0.01f
        for (m in magnitudes) if (m > maxMag) maxMag = m
        val norm = magnitudes.map { (it / maxMag).coerceIn(0f, 1f) }

        val binHz = samplingRate.toFloat() / (n * 2)
        fun bandEnergy(fromHz: Float, toHz: Float): Float {
            val fromBin = (fromHz / binHz).toInt().coerceIn(0, n - 1)
            val toBin = (toHz / binHz).toInt().coerceIn(fromBin, n - 1)
            var sum = 0f
            for (i in fromBin..toBin) sum += norm[i]
            val count = (toBin - fromBin + 1).coerceAtLeast(1)
            return (sum / count).coerceIn(0f, 1f)
        }

        val low = bandEnergy(20f, 250f)
        val mid = bandEnergy(250f, 4000f)
        val high = bandEnergy(4000f, 16000f)
        val overall = (low * 0.4f + mid * 0.4f + high * 0.2f).coerceIn(0f, 1f)
        // 底部噪声地板：静音时保持极低值而非 0
        return SpectrumData(
            low = max(low, 0.02f), mid = max(mid, 0.02f), high = max(high, 0.02f),
            overall = max(overall, 0.02f)
        )
    }

    fun stop() {
        isListening = false
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
    }
}
