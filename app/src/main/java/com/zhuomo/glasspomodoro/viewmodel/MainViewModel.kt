package com.zhuomo.glasspomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhuomo.glasspomodoro.audio.AudioAmplitudeDetector
import com.zhuomo.glasspomodoro.data.local.AppDatabase
import com.zhuomo.glasspomodoro.data.local.FocusRecordEntity
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.model.AppMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val settingsRepo = SettingsRepository(application)
    val db = AppDatabase.getInstance(application)
    val focusDao = db.focusDao()
    private val audioDetector = AudioAmplitudeDetector(application)

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _currentMode = MutableStateFlow(AppMode.CLOCK)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    init { startAudio() }

    private fun startAudio() {
        viewModelScope.launch {
            audioDetector.startListening().collect { _amplitude.value = it }
        }
    }

    fun switchMode(mode: AppMode) { _currentMode.value = mode }
    fun toggleSettings() { _showSettings.value = !_showSettings.value }

    fun saveFocusRecord(durationMinutes: Int) {
        viewModelScope.launch {
            focusDao.insert(FocusRecordEntity(durationMinutes = durationMinutes))
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioDetector.stop()
    }
}
