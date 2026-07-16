package com.zhuomo.glasspomodoro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhuomo.glasspomodoro.audio.AudioAmplitudeDetector
import com.zhuomo.glasspomodoro.model.PomodoroState
import com.zhuomo.glasspomodoro.model.SessionType
import com.zhuomo.glasspomodoro.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private val audioDetector = AudioAmplitudeDetector(application)
    private var timerJob: Job? = null
    private var audioJob: Job? = null

    init {
        checkPermission()
        startAudioListening()
    }

    private fun checkPermission() {
        _state.value = _state.value.copy(
            hasMicrophonePermission = audioDetector.hasPermission()
        )
    }

    fun onPermissionGranted() {
        _state.value = _state.value.copy(hasMicrophonePermission = true)
        restartAudio()
    }

    // ===== 计时器控制 =====

    fun startTimer() {
        if (_state.value.timerState == TimerState.IDLE ||
            _state.value.timerState == TimerState.FINISHED
        ) {
            // 重置为新会话
            val totalSecs = _state.value.sessionType.defaultMinutes * 60
            _state.value = _state.value.copy(
                timerState = TimerState.RUNNING,
                remainingSeconds = totalSecs,
                elapsedSeconds = 0,
                totalSeconds = totalSecs
            )
        } else if (_state.value.timerState == TimerState.PAUSED) {
            _state.value = _state.value.copy(timerState = TimerState.RUNNING)
        }

        startAudioListening()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerState == TimerState.RUNNING &&
                _state.value.remainingSeconds > 0
            ) {
                delay(1000L)
                val current = _state.value
                _state.value = current.copy(
                    remainingSeconds = current.remainingSeconds - 1,
                    elapsedSeconds = current.elapsedSeconds + 1
                )
            }
            // 计时结束
            if (_state.value.remainingSeconds <= 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(timerState = TimerState.PAUSED)
        timerJob?.cancel()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val totalSecs = _state.value.sessionType.defaultMinutes * 60
        _state.value = _state.value.copy(
            timerState = TimerState.IDLE,
            remainingSeconds = totalSecs,
            elapsedSeconds = 0,
            totalSeconds = totalSecs
        )
    }

    private fun onTimerFinished() {
        val current = _state.value
        var completed = current.completedSessions
        var nextSession = current.sessionType
        var nextSessionNum = current.currentSessionNumber

        if (current.isWorkSession) {
            // 专注完成
            completed++
            nextSessionNum++

            // 判断是短休息还是长休息
            nextSession = if (nextSessionNum > PomodoroState.SESSIONS_BEFORE_LONG_BREAK) {
                nextSessionNum = 1
                SessionType.LONG_BREAK
            } else {
                SessionType.SHORT_BREAK
            }
        } else {
            // 休息完成 -> 进入下一个专注
            nextSession = SessionType.WORK
        }

        val totalSecs = nextSession.defaultMinutes * 60
        _state.value = current.copy(
            timerState = TimerState.FINISHED,
            sessionType = nextSession,
            remainingSeconds = totalSecs,
            elapsedSeconds = 0,
            totalSeconds = totalSecs,
            completedSessions = completed,
            currentSessionNumber = nextSessionNum
        )
    }

    // ===== 音频检测 =====

    private fun startAudioListening() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            audioDetector.startListening().collect { amplitude ->
                _state.value = _state.value.copy(amplitude = amplitude)
            }
        }
    }

    private fun restartAudio() {
        audioDetector.stop()
        startAudioListening()
    }

    // ===== 会话切换 =====

    fun switchSession(type: SessionType) {
        if (_state.value.timerState == TimerState.RUNNING) return
        timerJob?.cancel()
        val totalSecs = type.defaultMinutes * 60
        _state.value = _state.value.copy(
            sessionType = type,
            timerState = TimerState.IDLE,
            remainingSeconds = totalSecs,
            elapsedSeconds = 0,
            totalSeconds = totalSecs
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioJob?.cancel()
        audioDetector.stop()
    }
}
