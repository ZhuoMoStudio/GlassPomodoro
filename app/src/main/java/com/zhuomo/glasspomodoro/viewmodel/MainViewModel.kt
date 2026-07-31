package com.zhuomo.glasspomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhuomo.glasspomodoro.audio.AudioAmplitudeDetector
import com.zhuomo.glasspomodoro.audio.AudioSpectrumAnalyzer
import com.zhuomo.glasspomodoro.data.local.AppDatabase
import com.zhuomo.glasspomodoro.data.local.FocusRecordEntity
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.media.MediaPlaybackMonitor
import com.zhuomo.glasspomodoro.model.AppMode
import com.zhuomo.glasspomodoro.model.SpectrumData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val settingsRepo = SettingsRepository(application)
    val db = AppDatabase.getInstance(application)
    val focusDao = db.focusDao()

    private val audioDetector = AudioAmplitudeDetector(application)
    private val spectrumAnalyzer = AudioSpectrumAnalyzer(application)
    val mediaMonitor = MediaPlaybackMonitor(application)

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    // v2.0 模块A：三频段频谱数据（低频→波幅、中频→波纹密度、高频→粒子）
    private val _spectrum = MutableStateFlow(SpectrumData.EMPTY)
    val spectrum: StateFlow<SpectrumData> = _spectrum.asStateFlow()

    private val _currentMode = MutableStateFlow(AppMode.CLOCK)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // 由 Compose 端主动调用，避免 ViewModel 初始化时阻塞
    fun startAudioMonitoring() {
        viewModelScope.launch {
            try {
                audioDetector.startListening().collect { _amplitude.value = it }
            } catch (_: Exception) {
                // 音频监听失败不影响核心功能
            }
        }
    }

    /** v2.0: 启动 FFT 频谱分析（与振幅检测并行，互不干扰） */
    fun startSpectrumMonitoring() {
        viewModelScope.launch {
            try {
                spectrumAnalyzer.startListening().collect { _spectrum.value = it }
            } catch (_: Exception) {
                // 频谱分析失败不影响核心功能
            }
        }
    }

    fun startMediaMonitoring() {
        viewModelScope.launch {
            try {
                mediaMonitor.startMonitoring().collect { /* 自动更新 nowPlaying 状态 */ }
            } catch (_: Exception) {
                // 媒体监听失败不影响核心功能
            }
        }
    }

    fun switchMode(mode: AppMode) { _currentMode.value = mode }
    fun toggleSettings() { _showSettings.value = !_showSettings.value }

    fun saveFocusRecord(durationMinutes: Int) {
        viewModelScope.launch {
            try {
                focusDao.insert(FocusRecordEntity(durationMinutes = durationMinutes))
            } catch (_: Exception) { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try { audioDetector.stop() } catch (_: Exception) { }
        try { spectrumAnalyzer.stop() } catch (_: Exception) { }
        try { mediaMonitor.stop() } catch (_: Exception) { }
    }
}
