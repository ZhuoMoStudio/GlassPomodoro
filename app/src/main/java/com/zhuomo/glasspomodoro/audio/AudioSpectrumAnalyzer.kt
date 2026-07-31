package com.zhuomo.glasspomodoro.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import com.zhuomo.glasspomodoro.model.SpectrumData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * v2.0.2: 实时频谱分析器 — 三通道自动切换
 *
 * 数据源优先级（保证"频响随声音振动"）：
 *  1. Visualizer 附着到【内部音频会话】（内置白噪音 MediaPlayer 的 AudioSessionId）
 *     —— 应用内声音 100% 可捕获，无需麦克风
 *  2. Visualizer 附着到【系统输出混音】(session 0)
 *     —— 捕获外部音乐 App 的播放音频（需 RECORD_AUDIO 权限）
 *  3. AudioRecord 麦克风 FFT（兜底）
 *     —— Visualizer 不可用（部分厂商设备/模拟器）时，用内置迭代 FFT 分析环境声
 *
 * FFT 频段映射：
 * - 低频 (20~250Hz)   → 波幅 A0
 * - 中频 (250~4kHz)   → 波纹密度/速度 (ω、k)
 * - 高频 (4kHz~16kHz) → 粒子/纹理强度
 */
class AudioSpectrumAnalyzer(private val context: Context) {

    companion object {
        private const val CAPTURE_SIZE = 1024
        private const val SMOOTHING = 0.5f
        private const val MIC_SAMPLE_RATE = 44100
        private const val MIC_BUFFER_SIZE = 1024
    }

    private val _spectrum = MutableStateFlow(SpectrumData.EMPTY)
    val spectrum: StateFlow<SpectrumData> = _spectrum.asStateFlow()

    private var visualizer: Visualizer? = null
    private var audioRecord: AudioRecord? = null

    @Volatile
    private var preferredSessionId: Int? = null

    @Volatile
    private var isListening = false

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    /**
     * 设置优先附着的音频会话（内置白噪音 MediaPlayer 的 session）。
     * 传入 null 时监听系统输出混音（外部音乐）。运行时动态切换无需重启。
     */
    fun setPreferredSession(sessionId: Int?) {
        preferredSessionId = sessionId
    }

    /**
     * 开始监听并持续输出频段数据流
     */
    fun startListening(): Flow<SpectrumData> = flow {
        if (!hasPermission()) { emit(SpectrumData.EMPTY); return@flow }
        isListening = true

        try {
            // ===== 阶段 1/2：Visualizer 路径（内部会话 → 输出混音 动态切换） =====
            var currentSession: Int? = null
            var viz: Visualizer? = null
            var mic: AudioRecord? = null
            var micRe: FloatArray? = null
            var micIm: FloatArray? = null
            val micBuf = ShortArray(MIC_BUFFER_SIZE)

            while (isListening) {
                val target = preferredSessionId ?: 0
                if (target != currentSession) {
                    // 会话变化 → 重建 Visualizer
                    try { viz?.enabled = false; viz?.release() } catch (_: Exception) {}
                    viz = try { createVisualizer(target) } catch (_: Exception) { null }
                    currentSession = target
                }

                // Visualizer 不可用 → 尝试麦克风 FFT
                if (viz == null && mic == null) {
                    mic = try { createMicRecord() } catch (_: Exception) { null }
                    if (mic != null) {
                        micRe = FloatArray(MIC_BUFFER_SIZE)
                        micIm = FloatArray(MIC_BUFFER_SIZE)
                    }
                }

                if (mic != null) {
                    // ===== 麦克风 FFT 路径 =====
                    val read = mic.read(micBuf, 0, MIC_BUFFER_SIZE)
                    if (read > 0) {
                        val re = micRe!!; val im = micIm!!
                        for (i in 0 until read) { re[i] = micBuf[i] / 32768f; im[i] = 0f }
                        Fft.fft(re, im)
                        _spectrum.value = analyzeMagnitudes(re, im, read, MIC_SAMPLE_RATE)
                    }
                }

                emit(smooth(_spectrum.value))
                delay(32)
            }
        } catch (_: Exception) {
            emit(SpectrumData.EMPTY)
        } finally {
            try { visualizerCleanup() } catch (_: Exception) {}
            try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
            audioRecord = null
        }
    }.flowOn(Dispatchers.IO)

    /** 创建 Visualizer 并挂接 FFT 回调（session 0 = 输出混音） */
    private fun createVisualizer(session: Int): Visualizer {
        val capSize = Visualizer.getCaptureSizeRange().let { it[1].coerceAtMost(CAPTURE_SIZE) }
        return Visualizer(session).apply {
            enabled = false
            captureSize = capSize
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                    // 波形数据备用，此处不消费
                }

                override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    fft?.let { _spectrum.value = analyzeFft(it, samplingRate) }
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true)
            enabled = true
        }
    }

    /** 创建麦克风录音（兜底路径） */
    private fun createMicRecord(): AudioRecord? {
        val minBuf = AudioRecord.getMinBufferSize(
            MIC_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val bufSize = maxOf(minBuf, MIC_BUFFER_SIZE * 2)
        val rec = AudioRecord(
            MediaRecorder.AudioSource.MIC, MIC_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize
        )
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); return null }
        rec.startRecording()
        audioRecord = rec
        return rec
    }

    private fun visualizerCleanup() {
        try { visualizer?.enabled = false; visualizer?.release() } catch (_: Exception) {}
        visualizer = null
    }

    private fun smooth(prev: SpectrumData): SpectrumData = SpectrumData(
        low = prev.low * SMOOTHING + _spectrum.value.low * (1f - SMOOTHING),
        mid = prev.mid * SMOOTHING + _spectrum.value.mid * (1f - SMOOTHING),
        high = prev.high * SMOOTHING + _spectrum.value.high * (1f - SMOOTHING),
        overall = prev.overall * SMOOTHING + _spectrum.value.overall * (1f - SMOOTHING)
    )

    /**
     * Visualizer FFT 字节数组 → 三频段能量
     * fft 格式: 每对字节 (实部, 虚部)，从 0Hz 开始
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
        return bandEnergies(magnitudes, samplingRate / (n * 2f))
    }

    /** 麦克风 FFT 结果 → 三频段能量 */
    private fun analyzeMagnitudes(re: FloatArray, im: FloatArray, n: Int, samplingRate: Int): SpectrumData {
        val half = n / 2
        if (half < 2) return SpectrumData.EMPTY
        val magnitudes = FloatArray(half) { i ->
            val mag = sqrt(re[i] * re[i] + im[i] * im[i])
            if (mag > 0) (20.0 * ln(mag + 1e-6) / 20.0).toFloat() else 0f
        }
        return bandEnergies(magnitudes, samplingRate / n.toFloat())
    }

    /** 频段能量计算（低频→波幅、中频→密度、高频→粒子） */
    private fun bandEnergies(magnitudes: FloatArray, binHz: Float): SpectrumData {
        var maxMag = 0.01f
        for (m in magnitudes) if (m > maxMag) maxMag = m
        fun bandEnergy(fromHz: Float, toHz: Float): Float {
            val fromBin = (fromHz / binHz).toInt().coerceIn(0, magnitudes.size - 1)
            val toBin = (toHz / binHz).toInt().coerceIn(fromBin, magnitudes.size - 1)
            var sum = 0f
            for (i in fromBin..toBin) sum += (magnitudes[i] / maxMag).coerceIn(0f, 1f)
            return (sum / (toBin - fromBin + 1)).coerceIn(0f, 1f)
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
        visualizerCleanup()
        try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
        audioRecord = null
    }
}

/**
 * 迭代式 Cooley-Tukey FFT（原地、非递归）
 * 参考经典开源实现（MIT 协议，见 LICENSE THIRD-PARTY NOTICES）
 */
object Fft {
    fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        require(n > 0 && n and (n - 1) == 0) { "FFT size must be power of 2" }
        if (n == 1) return

        // 位反转重排
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                var tmp = re[i]; re[i] = re[j]; re[j] = tmp
                tmp = im[i]; im[i] = im[j]; im[j] = tmp
            }
        }

        // 蝶形运算
        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wRe = cos(angle).toFloat()
            val wIm = sin(angle).toFloat()
            for (i in 0 until n step len) {
                var curRe = 1f
                var curIm = 0f
                val half = len / 2
                for (k in 0 until half) {
                    val uRe = re[i + k]
                    val uIm = im[i + k]
                    val vRe = re[i + k + half] * curRe - im[i + k + half] * curIm
                    val vIm = re[i + k + half] * curIm + im[i + k + half] * curRe
                    re[i + k] = uRe + vRe
                    im[i + k] = uIm + vIm
                    re[i + k + half] = uRe - vRe
                    im[i + k + half] = uIm - vIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
            }
            len = len shl 1
        }
    }
}
