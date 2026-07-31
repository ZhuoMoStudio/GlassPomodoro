package com.zhuomo.glasspomodoro.model

// ============================================================
// GlassPomodoro v2.0 新增模型
// 物理级声学视觉系统 + 专辑封面动态壁纸 + 玻璃质感 + GitHub云能力
// ============================================================

// ===== 模块A：声学视觉 =====

/** 波动显示模式 */
enum class WaveMode(val labelZh: String, val labelEn: String) {
    PURE_RIPPLE("纯水波", "Pure Ripple"),
    BOTTOM_SPECTRUM("底部频谱", "Spectrum"),
    HYBRID("混合模式", "Hybrid")
}

/** 波源位置 */
enum class WaveSourcePosition(val labelZh: String, val labelEn: String) {
    CENTER("中心", "Center"),
    BOTTOM_LEFT("左下", "Bottom Left"),
    BOTTOM_RIGHT("右下", "Bottom Right"),
    CUSTOM("自定义", "Custom")
}

/**
 * 声学视觉参数
 * 物理波形模型: A(d) = A0 × e^(-decay·d) × sin(ωt - k·d)
 */
data class AcousticSettings(
    val mode: WaveMode = WaveMode.HYBRID,
    val sourcePosition: WaveSourcePosition = WaveSourcePosition.CENTER,
    val customSourceX: Float = 0.25f,   // 自定义波源 X (0~1 归一化)
    val customSourceY: Float = 0.7f,    // 自定义波源 Y (0~1 归一化)
    val amplitudeStrength: Float = 1.0f, // 振幅强度 (0.2~2.0)
    val waveSpeed: Float = 1.0f,         // 波速 (0.3~3.0)
    val decay: Float = 1.2f              // 衰减系数 (0.3~3.0)
) {
    fun sourcePoint(): Pair<Float, Float> = when (sourcePosition) {
        WaveSourcePosition.CENTER -> 0.5f to 0.5f
        WaveSourcePosition.BOTTOM_LEFT -> 0.12f to 0.85f
        WaveSourcePosition.BOTTOM_RIGHT -> 0.88f to 0.85f
        WaveSourcePosition.CUSTOM -> customSourceX to customSourceY
    }
}

/** FFT 频段数据（三频段映射：低频→波幅、中频→波纹密度、高频→粒子/纹理） */
data class SpectrumData(
    val low: Float = 0f,     // 低频能量 0~1
    val mid: Float = 0f,     // 中频能量 0~1
    val high: Float = 0f,    // 高频能量 0~1
    val overall: Float = 0f  // 总能量 0~1
) {
    companion object { val EMPTY = SpectrumData() }
}

// ===== 模块B：壁纸源优先级 =====

/** 壁纸源（可排序优先级） */
enum class WallpaperPrioritySource(val labelZh: String, val labelEn: String, val icon: String) {
    ALBUM_ART("专辑封面", "Album Art", "🎵"),
    BING("Bing每日壁纸", "Bing Daily", "🌐"),
    LOCAL("本地相册", "Local Gallery", "🖼️"),
    SOLID_COLOR("纯色背景", "Solid Color", "🎨")
}

/** 壁纸优先级配置（有序列表） */
data class WallpaperPrioritySettings(
    val order: List<WallpaperPrioritySource> = listOf(
        WallpaperPrioritySource.ALBUM_ART,
        WallpaperPrioritySource.BING,
        WallpaperPrioritySource.LOCAL,
        WallpaperPrioritySource.SOLID_COLOR
    )
)

// ===== 模块C：玻璃质感 =====

/** 性能档位 */
enum class PerformanceProfile(val labelZh: String, val labelEn: String) {
    HIGH("高性能", "High"),
    BALANCED("均衡", "Balanced"),
    POWER_SAVE("省电", "Power Save")
}

/** 玻璃效果设置 */
data class GlassSettings(
    val blurStrength: Float = 6f,       // 毛玻璃模糊强度 (0~24)
    val lightAngle: Float = 0.6f,       // 光影角度 0~1（映射 0°~360°）
    val showHighlight: Boolean = true   // 玻璃高光扫描
)

// ===== 模块E：GitHub 云能力 =====

/** GitHub Token 状态 */
enum class GitHubTokenState(val labelZh: String, val labelEn: String) {
    NOT_CONFIGURED("未配置", "Not Configured"),
    VERIFYING("验证中…", "Verifying…"),
    VERIFIED("已验证", "Verified"),
    INVALID("无效", "Invalid")
}

/** 首次启动设置 */
data class FirstLaunchSettings(val isFirstLaunch: Boolean = true)
