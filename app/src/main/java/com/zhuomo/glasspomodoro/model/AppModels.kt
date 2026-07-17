package com.zhuomo.glasspomodoro.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// ===== 时钟显示设置 =====
data class ClockDisplaySettings(
    val showYear: Boolean = false,
    val showDate: Boolean = true,
    val showWeekday: Boolean = true,
    val showSeconds: Boolean = true,
    val use24Hour: Boolean = true
)

// ===== 时钟字体枚举 =====
enum class ClockFont(val labelZh: String, val labelEn: String, val fontName: String) {
    MONO("等宽", "Mono", "monospace"),
    SANS("无衬线", "Sans", "sans-serif"),
    SERIF("衬线", "Serif", "serif"),
    MODERN("现代", "Modern", "sans-serif-light"),
    BOLD("粗体", "Bold", "sans-serif-medium")
}

// ===== 时钟自定义颜色 =====
data class ClockCustomColors(
    val usePreset: Boolean = true,
    val customColor: Long = Color(0xFF6C63FF).toArgb().toLong(),
    val customSecondaryColor: Long = Color(0xFF339AF0).toArgb().toLong()
)

// ===== 壁纸设置 =====
enum class WallpaperSource { NONE, BING, LOCAL, ALBUM_ART }
data class WallpaperSettings(
    val source: WallpaperSource = WallpaperSource.BING,
    val localPath: String = "",
    val bingRegion: String = "zh-CN",
    val blurAmount: Float = 0f
)

// ===== 配色方案 =====
data class ColorPreset(
    val name: String,
    val nameEn: String,
    val primary: Color,
    val secondary: Color,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val accent1: Color,
    val accent2: Color
)

object ColorPresets {
    val presets = listOf(
        ColorPreset("星夜", "Starry Night",
            Color(0xFF6C63FF), Color(0xFF339AF0),
            Color(0xFF0D0D2B), Color(0xFF1A1A4E),
            Color(0xFFFF6B6B), Color(0xFF51CF66)),
        ColorPreset("极光", "Aurora",
            Color(0xFF00B4D8), Color(0xFF51CF66),
            Color(0xFF001233), Color(0xFF003566),
            Color(0xFF00F5D4), Color(0xFF00BBF9)),
        ColorPreset("落日", "Sunset",
            Color(0xFFFF6B6B), Color(0xFFFFA94D),
            Color(0xFF2D1B00), Color(0xFF5C3A00),
            Color(0xFFFF6B6B), Color(0xFFFFD93D)),
        ColorPreset("樱花", "Sakura",
            Color(0xFFFF7EB3), Color(0xFFD4A5FF),
            Color(0xFF2D1B2E), Color(0xFF4A2D4A),
            Color(0xFFFF7EB3), Color(0xFFD4A5FF)),
        ColorPreset("薄荷", "Mint",
            Color(0xFF51CF66), Color(0xFF20C997),
            Color(0xFF002B1A), Color(0xFF004D33),
            Color(0xFF51CF66), Color(0xFF20C997)),
        ColorPreset("霓虹", "Neon",
            Color(0xFF00FFFF), Color(0xFFFF00FF),
            Color(0xFF0A0015), Color(0xFF1A0033),
            Color(0xFF00FFFF), Color(0xFFFF00FF)),
    )
}

// ===== 主题模式 =====
enum class ThemeMode(val labelZh: String, val labelEn: String) {
    DARK("深色", "Dark"),
    LIGHT("浅色", "Light"),
    SYSTEM("跟随系统", "System")
}

// ===== 主题设置 =====
data class ThemeSettings(
    val presetIndex: Int = 0,
    val isCustomColor: Boolean = false,
    val customPrimary: Long = Color(0xFF6C63FF).toArgb().toLong(),
    val customSecondary: Long = Color(0xFF339AF0).toArgb().toLong(),
    val themeMode: ThemeMode = ThemeMode.DARK
) {
    fun getPreset(): ColorPreset = ColorPresets.presets.getOrElse(presetIndex) { ColorPresets.presets[0] }
}

// ===== 蒙版层样式枚举 =====
enum class DimMaskStyle(val labelZh: String, val labelEn: String) {
    RADIAL_GRADIENT("径向渐变", "Radial Gradient"),
    DYNAMIC_GLOW("动态光晕", "Dynamic Glow"),
    FROSTED_GLASS("毛玻璃", "Frosted Glass"),
    TECH_GRID("科技网格", "Tech Grid")
}

// ===== 蒙版设置 =====
data class DimMaskSettings(
    val style: DimMaskStyle = DimMaskStyle.DYNAMIC_GLOW,
    val customAlpha: Float = 0.35f,
    val dynamicResponse: Float = 0.5f
)

// ===== 番茄钟状态 =====
enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }
enum class SessionType(val labelZh: String, val labelEn: String, val defaultMinutes: Int) {
    WORK("专注", "Focus", 25),
    SHORT_BREAK("短休息", "Break", 5),
    LONG_BREAK("长休息", "Long Break", 15)
}

data class PomodoroState(
    val timerState: TimerState = TimerState.IDLE,
    val sessionType: SessionType = SessionType.WORK,
    val remainingSeconds: Int = SessionType.WORK.defaultMinutes * 60,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = SessionType.WORK.defaultMinutes * 60,
    val completedSessions: Int = 0,
    val currentSessionNumber: Int = 1,
    val amplitude: Float = 0f,
    val hasMicPermission: Boolean = false
) {
    val progress: Float get() = if (totalSeconds > 0) 1f - remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val formattedTime: String get() = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)
    val isWorkSession: Boolean get() = sessionType == SessionType.WORK
    companion object { const val SESSIONS_BEFORE_LONG_BREAK = 4 }
}

data class FocusRecord(val id: Long = 0, val timestamp: Long = System.currentTimeMillis(), val durationMinutes: Int = 25, val completed: Boolean = true)

// ===== 白噪音 =====
data class WhiteNoiseTrack(val nameZh: String, val nameEn: String, val icon: String, val resRaw: String = "")
object WhiteNoiseTracks {
    val tracks = listOf(
        WhiteNoiseTrack("雨声", "Rain", "🌧", "rain"),
        WhiteNoiseTrack("海浪", "Ocean Waves", "🌊", "ocean"),
        WhiteNoiseTrack("篝火", "Campfire", "🔥", "fire"),
        WhiteNoiseTrack("森林", "Forest", "🌲", "forest"),
        WhiteNoiseTrack("溪流", "Stream", "💧", "stream"),
        WhiteNoiseTrack("白噪音", "White Noise", "📡", "whitenoise"),
    )
}

// ===== 应用模式 =====
enum class AppMode(val icon: String, val labelZh: String, val labelEn: String) {
    CLOCK("🕐", "时钟", "Clock"),
    POMODORO("🍅", "专注", "Focus")
}
