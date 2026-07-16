package com.zhuomo.glasspomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuomo.glasspomodoro.model.PomodoroState
import com.zhuomo.glasspomodoro.model.SessionType
import com.zhuomo.glasspomodoro.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun updateAmplitude(value: Float) { _state.value = _state.value.copy(amplitude = value) }
    fun setMicPermission(granted: Boolean) { _state.value = _state.value.copy(hasMicPermission = granted) }

    fun startTimer() {
        val current = _state.value
        if (current.timerState == TimerState.IDLE || current.timerState == TimerState.FINISHED) {
            val total = current.sessionType.defaultMinutes * 60
            _state.value = current.copy(timerState = TimerState.RUNNING, remainingSeconds = total, elapsedSeconds = 0, totalSeconds = total)
        } else if (current.timerState == TimerState.PAUSED) {
            _state.value = current.copy(timerState = TimerState.RUNNING)
        }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerState == TimerState.RUNNING && _state.value.remainingSeconds > 0) {
                delay(1000L)
                _state.value = _state.value.copy(
                    remainingSeconds = _state.value.remainingSeconds - 1,
                    elapsedSeconds = _state.value.elapsedSeconds + 1
                )
            }
            if (_state.value.remainingSeconds <= 0) onTimerFinished()
        }
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(timerState = TimerState.PAUSED)
        timerJob?.cancel()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val total = _state.value.sessionType.defaultMinutes * 60
        _state.value = _state.value.copy(timerState = TimerState.IDLE, remainingSeconds = total, elapsedSeconds = 0, totalSeconds = total)
    }

    fun switchSession(type: SessionType) {
        if (_state.value.timerState == TimerState.RUNNING) return
        timerJob?.cancel()
        val total = type.defaultMinutes * 60
        _state.value = _state.value.copy(sessionType = type, timerState = TimerState.IDLE, remainingSeconds = total, elapsedSeconds = 0, totalSeconds = total)
    }

    private fun onTimerFinished() {
        val cur = _state.value
        var completed = cur.completedSessions
        var next = cur.sessionType
        var num = cur.currentSessionNumber
        if (cur.isWorkSession) {
            completed++
            num++
            next = if (num > PomodoroState.SESSIONS_BEFORE_LONG_BREAK) {
                num = 1; SessionType.LONG_BREAK
            } else SessionType.SHORT_BREAK
        } else { next = SessionType.WORK }
        val total = next.defaultMinutes * 60
        _state.value = cur.copy(
            timerState = TimerState.FINISHED, sessionType = next,
            remainingSeconds = total, totalSeconds = total,
            completedSessions = completed, currentSessionNumber = num
        )
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
