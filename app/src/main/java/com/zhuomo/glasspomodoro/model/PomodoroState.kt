package com.zhuomo.glasspomodoro.model

/**
 * 番茄钟计时器状态
 */
enum class TimerState {
    IDLE,       // 初始状态
    RUNNING,    // 运行中
    PAUSED,     // 暂停
    FINISHED    // 完成
}

/**
 * 会话类型
 */
enum class SessionType(val label: String, val defaultMinutes: Int) {
    WORK("专注", 25),
    SHORT_BREAK("短休息", 5),
    LONG_BREAK("长休息", 15)
}

/**
 * 番茄钟完整状态
 */
data class PomodoroState(
    val timerState: TimerState = TimerState.IDLE,
    val sessionType: SessionType = SessionType.WORK,
    val remainingSeconds: Int = SessionType.WORK.defaultMinutes * 60,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = SessionType.WORK.defaultMinutes * 60,
    val completedSessions: Int = 0,
    val currentSessionNumber: Int = 1,    // 当前第几轮 (1-4)
    val amplitude: Float = 0f,            // 当前音频振幅 (0..1)
    val hasMicrophonePermission: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
        } else 0f

    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val isWorkSession: Boolean
        get() = sessionType == SessionType.WORK

    companion object {
        const val SESSIONS_BEFORE_LONG_BREAK = 4
    }
}
