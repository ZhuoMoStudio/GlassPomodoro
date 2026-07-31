package com.zhuomo.glasspomodoro.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.zhuomo.glasspomodoro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val SHOW_YEAR = booleanPreferencesKey("show_year"); val SHOW_DATE = booleanPreferencesKey("show_date"); val SHOW_WEEKDAY = booleanPreferencesKey("show_weekday")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds"); val USE_24HOUR = booleanPreferencesKey("use_24hour")
        val WALLPAPER_SOURCE = stringPreferencesKey("wallpaper_source"); val LOCAL_PATH = stringPreferencesKey("local_path"); val BING_REGION = stringPreferencesKey("bing_region"); val BLUR_AMOUNT = floatPreferencesKey("blur_amount")
        val PRESET_INDEX = intPreferencesKey("preset_index"); val IS_CUSTOM_COLOR = booleanPreferencesKey("is_custom_color")
        val CUSTOM_PRIMARY = longPreferencesKey("custom_primary"); val CUSTOM_SECONDARY = longPreferencesKey("custom_secondary"); val THEME_MODE = stringPreferencesKey("theme_mode"); val LANGUAGE = stringPreferencesKey("language")
        val CLOCK_FONT = stringPreferencesKey("clock_font"); val CLOCK_USE_PRESET = booleanPreferencesKey("clock_use_preset")
        val CLOCK_CUSTOM_COLOR = longPreferencesKey("clock_custom_color"); val CLOCK_CUSTOM_SECONDARY = longPreferencesKey("clock_custom_secondary")
        val DIM_MASK_STYLE = stringPreferencesKey("dim_mask_style"); val DIM_MASK_ALPHA = floatPreferencesKey("dim_mask_alpha"); val DIM_MASK_RESPONSE = floatPreferencesKey("dim_mask_response")
        // v1.0.6 动效开关
        val FX_WATER_RIPPLE = booleanPreferencesKey("fx_water_ripple"); val FX_WAVEFORM = booleanPreferencesKey("fx_waveform"); val FX_FLUID_PARTICLES = booleanPreferencesKey("fx_fluid_particles")
        val FX_WAVE_AMP = floatPreferencesKey("fx_wave_amplification"); val FX_RIPPLE_AMP = floatPreferencesKey("fx_ripple_amplification")
        // v2.0 声学视觉
        val ACOUSTIC_MODE = stringPreferencesKey("acoustic_mode"); val ACOUSTIC_SOURCE_POS = stringPreferencesKey("acoustic_source_pos")
        val ACOUSTIC_SOURCE_X = floatPreferencesKey("acoustic_source_x"); val ACOUSTIC_SOURCE_Y = floatPreferencesKey("acoustic_source_y")
        val ACOUSTIC_AMP = floatPreferencesKey("acoustic_amp"); val ACOUSTIC_SPEED = floatPreferencesKey("acoustic_speed"); val ACOUSTIC_DECAY = floatPreferencesKey("acoustic_decay")
        // v2.0 壁纸源优先级（逗号分隔的有序列表）
        val WALLPAPER_PRIORITY = stringPreferencesKey("wallpaper_priority")
        // v2.0 玻璃效果与性能
        val GLASS_BLUR = floatPreferencesKey("glass_blur"); val GLASS_LIGHT_ANGLE = floatPreferencesKey("glass_light_angle"); val GLASS_HIGHLIGHT = booleanPreferencesKey("glass_highlight")
        val PERFORMANCE_PROFILE = stringPreferencesKey("performance_profile")
        // v2.0 GitHub & 首次启动
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch"); val GITHUB_TOKEN_SET = booleanPreferencesKey("github_token_set")
        val GITHUB_USERNAME = stringPreferencesKey("github_username"); val GITHUB_AVATAR_URL = stringPreferencesKey("github_avatar_url")
    }

    val clockSettings: Flow<ClockDisplaySettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> ClockDisplaySettings(p[SHOW_YEAR]?:false, p[SHOW_DATE]?:true, p[SHOW_WEEKDAY]?:true, p[SHOW_SECONDS]?:true, p[USE_24HOUR]?:true) }.flowOn(Dispatchers.IO)
    val wallpaperSettings: Flow<WallpaperSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> WallpaperSettings(try{ WallpaperSource.valueOf(p[WALLPAPER_SOURCE] ?: "BING") }catch(_:Exception){WallpaperSource.BING}, p[LOCAL_PATH]?: "", p[BING_REGION]?: "zh-CN", p[BLUR_AMOUNT]?:0f) }.flowOn(Dispatchers.IO)
    val themeSettings: Flow<ThemeSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> ThemeSettings(p[PRESET_INDEX]?:0, p[IS_CUSTOM_COLOR]?:false, p[CUSTOM_PRIMARY]?:0xFF6C63FF, p[CUSTOM_SECONDARY]?:0xFF339AF0, try { ThemeMode.valueOf(p[THEME_MODE]?: "DARK") } catch(_:Exception){ ThemeMode.DARK }) }.flowOn(Dispatchers.IO)
    val language: Flow<String> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { it[LANGUAGE] ?: "zh" }.flowOn(Dispatchers.IO)
    val dimMaskSettings: Flow<DimMaskSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> DimMaskStyle.valueOf(p[DIM_MASK_STYLE] ?: "DYNAMIC_GLOW").let { DimMaskSettings(it, p[DIM_MASK_ALPHA]?:0.35f, p[DIM_MASK_RESPONSE]?:0.5f) } }.flowOn(Dispatchers.IO)
    val clockFont: Flow<ClockFont> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> try { ClockFont.valueOf(p[CLOCK_FONT]?: "MONO") } catch(_:Exception){ ClockFont.MONO } }.flowOn(Dispatchers.IO)
    val clockColors: Flow<ClockCustomColors> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> ClockCustomColors(p[CLOCK_USE_PRESET]?:true, p[CLOCK_CUSTOM_COLOR]?:0xFF6C63FF, p[CLOCK_CUSTOM_SECONDARY]?:0xFF339AF0) }.flowOn(Dispatchers.IO)
    // v1.0.6 动效设置
    val visualEffects: Flow<VisualEffectsSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p -> VisualEffectsSettings(p[FX_WATER_RIPPLE]?:true, p[FX_WAVEFORM]?:true, p[FX_FLUID_PARTICLES]?:false, p[FX_WAVE_AMP]?:1.5f, p[FX_RIPPLE_AMP]?:1.8f) }.flowOn(Dispatchers.IO)

    // ===== v2.0 声学视觉设置 =====
    val acousticSettings: Flow<AcousticSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p ->
        AcousticSettings(
            mode = safeEnum(p[ACOUSTIC_MODE], WaveMode.HYBRID),
            sourcePosition = safeEnum(p[ACOUSTIC_SOURCE_POS], WaveSourcePosition.CENTER),
            customSourceX = (p[ACOUSTIC_SOURCE_X] ?: 0.25f).coerceIn(0f, 1f),
            customSourceY = (p[ACOUSTIC_SOURCE_Y] ?: 0.7f).coerceIn(0f, 1f),
            amplitudeStrength = (p[ACOUSTIC_AMP] ?: 1.0f).coerceIn(0.2f, 2f),
            waveSpeed = (p[ACOUSTIC_SPEED] ?: 1.0f).coerceIn(0.3f, 3f),
            decay = (p[ACOUSTIC_DECAY] ?: 1.2f).coerceIn(0.3f, 3f)
        )
    }.flowOn(Dispatchers.IO)

    // ===== v2.0 壁纸源优先级 =====
    val wallpaperPriority: Flow<WallpaperPrioritySettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p ->
        val raw = p[WALLPAPER_PRIORITY] ?: ""
        val order = if (raw.isBlank()) WallpaperPrioritySettings().order
        else raw.split(",").mapNotNull { name -> runCatching { WallpaperPrioritySource.valueOf(name) }.getOrNull() }
        WallpaperPrioritySettings(order.ifEmpty { WallpaperPrioritySettings().order })
    }.flowOn(Dispatchers.IO)

    // ===== v2.0 玻璃效果 & 性能 =====
    val glassSettings: Flow<GlassSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p ->
        GlassSettings(
            blurStrength = (p[GLASS_BLUR] ?: 6f).coerceIn(0f, 24f),
            lightAngle = (p[GLASS_LIGHT_ANGLE] ?: 0.6f).coerceIn(0f, 1f),
            showHighlight = p[GLASS_HIGHLIGHT] ?: true
        )
    }.flowOn(Dispatchers.IO)

    val performanceProfile: Flow<PerformanceProfile> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p -> safeEnum(p[PERFORMANCE_PROFILE], PerformanceProfile.BALANCED) }.flowOn(Dispatchers.IO)

    // ===== v2.0 GitHub 配置状态 =====
    val githubConfig: Flow<GitHubConfigState> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p ->
        GitHubConfigState(
            tokenConfigured = p[GITHUB_TOKEN_SET] ?: false,
            username = p[GITHUB_USERNAME] ?: "",
            avatarUrl = p[GITHUB_AVATAR_URL] ?: ""
        )
    }.flowOn(Dispatchers.IO)

    /** 首次启动标志 */
    val isFirstLaunch: Flow<FirstLaunchSettings> = ctx().catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p -> FirstLaunchSettings(p[FIRST_LAUNCH] ?: true) }.flowOn(Dispatchers.IO)

    suspend fun updateClock(s: ClockDisplaySettings) { context.dataStore.edit { p -> p[SHOW_YEAR]=s.showYear; p[SHOW_DATE]=s.showDate; p[SHOW_WEEKDAY]=s.showWeekday; p[SHOW_SECONDS]=s.showSeconds; p[USE_24HOUR]=s.use24Hour }}
    suspend fun updateWallpaper(s: WallpaperSettings) { context.dataStore.edit { p -> p[WALLPAPER_SOURCE]=s.source.name; p[LOCAL_PATH]=s.localPath; p[BING_REGION]=s.bingRegion; p[BLUR_AMOUNT]=s.blurAmount }}
    suspend fun updateTheme(s: ThemeSettings) { context.dataStore.edit { p -> p[PRESET_INDEX]=s.presetIndex; p[IS_CUSTOM_COLOR]=s.isCustomColor; p[CUSTOM_PRIMARY]=s.customPrimary; p[CUSTOM_SECONDARY]=s.customSecondary; p[THEME_MODE]=s.themeMode.name }}
    suspend fun updateDimMask(s: DimMaskSettings) { context.dataStore.edit { p -> p[DIM_MASK_STYLE]=s.style.name; p[DIM_MASK_ALPHA]=s.customAlpha; p[DIM_MASK_RESPONSE]=s.dynamicResponse }}
    suspend fun setLanguage(l: String) { context.dataStore.edit { it[LANGUAGE]=l } }
    suspend fun updateClockFont(f: ClockFont) { context.dataStore.edit { it[CLOCK_FONT]=f.name } }
    suspend fun updateClockColors(c: ClockCustomColors) { context.dataStore.edit { p -> p[CLOCK_USE_PRESET]=c.usePreset; p[CLOCK_CUSTOM_COLOR]=c.customColor; p[CLOCK_CUSTOM_SECONDARY]=c.customSecondaryColor }}
    // v1.0.6
    suspend fun updateVisualEffects(s: VisualEffectsSettings) { context.dataStore.edit { p -> p[FX_WATER_RIPPLE]=s.enableWaterRipple; p[FX_WAVEFORM]=s.enableWaveform; p[FX_FLUID_PARTICLES]=s.enableFluidParticles; p[FX_WAVE_AMP]=s.waveformAmplification; p[FX_RIPPLE_AMP]=s.rippleAmplification }}

    // ===== v2.0 =====
    suspend fun updateAcousticSettings(s: AcousticSettings) {
        context.dataStore.edit { p ->
            p[ACOUSTIC_MODE] = s.mode.name; p[ACOUSTIC_SOURCE_POS] = s.sourcePosition.name
            p[ACOUSTIC_SOURCE_X] = s.customSourceX; p[ACOUSTIC_SOURCE_Y] = s.customSourceY
            p[ACOUSTIC_AMP] = s.amplitudeStrength; p[ACOUSTIC_SPEED] = s.waveSpeed; p[ACOUSTIC_DECAY] = s.decay
        }
    }
    suspend fun updateWallpaperPriority(s: WallpaperPrioritySettings) {
        context.dataStore.edit { p -> p[WALLPAPER_PRIORITY] = s.order.joinToString(",") { it.name } }
    }
    suspend fun updateGlassSettings(s: GlassSettings) {
        context.dataStore.edit { p -> p[GLASS_BLUR] = s.blurStrength; p[GLASS_LIGHT_ANGLE] = s.lightAngle; p[GLASS_HIGHLIGHT] = s.showHighlight }
    }
    suspend fun updatePerformanceProfile(profile: PerformanceProfile) { context.dataStore.edit { p -> p[PERFORMANCE_PROFILE] = profile.name } }
    suspend fun completeFirstLaunch() { context.dataStore.edit { p -> p[FIRST_LAUNCH] = false } }
    suspend fun markGitHubConfigured(username: String, avatarUrl: String) {
        context.dataStore.edit { p -> p[GITHUB_TOKEN_SET] = true; p[GITHUB_USERNAME] = username; p[GITHUB_AVATAR_URL] = avatarUrl }
    }
    suspend fun clearGitHubConfig() { context.dataStore.edit { p -> p[GITHUB_TOKEN_SET] = false; p[GITHUB_USERNAME] = ""; p[GITHUB_AVATAR_URL] = "" } }

    private fun <T : Enum<T>> safeEnum(name: String?, default: T): T =
        if (name.isNullOrBlank()) default
        else runCatching { java.lang.Enum.valueOf(default.javaClass as Class<T>, name) }.getOrDefault(default)

    private fun ctx() = context.dataStore.data
}

/** v2.0: GitHub 配置状态（保存在 DataStore 的公开元数据 + 加密存储 Token） */
data class GitHubConfigState(
    val tokenConfigured: Boolean = false,
    val username: String = "",
    val avatarUrl: String = ""
)
