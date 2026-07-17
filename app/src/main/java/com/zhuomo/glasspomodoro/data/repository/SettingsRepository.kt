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

    suspend fun updateClock(s: ClockDisplaySettings) { context.dataStore.edit { p -> p[SHOW_YEAR]=s.showYear; p[SHOW_DATE]=s.showDate; p[SHOW_WEEKDAY]=s.showWeekday; p[SHOW_SECONDS]=s.showSeconds; p[USE_24HOUR]=s.use24Hour }}
    suspend fun updateWallpaper(s: WallpaperSettings) { context.dataStore.edit { p -> p[WALLPAPER_SOURCE]=s.source.name; p[LOCAL_PATH]=s.localPath; p[BING_REGION]=s.bingRegion; p[BLUR_AMOUNT]=s.blurAmount }}
    suspend fun updateTheme(s: ThemeSettings) { context.dataStore.edit { p -> p[PRESET_INDEX]=s.presetIndex; p[IS_CUSTOM_COLOR]=s.isCustomColor; p[CUSTOM_PRIMARY]=s.customPrimary; p[CUSTOM_SECONDARY]=s.customSecondary; p[THEME_MODE]=s.themeMode.name }}
    suspend fun updateDimMask(s: DimMaskSettings) { context.dataStore.edit { p -> p[DIM_MASK_STYLE]=s.style.name; p[DIM_MASK_ALPHA]=s.customAlpha; p[DIM_MASK_RESPONSE]=s.dynamicResponse }}
    suspend fun setLanguage(l: String) { context.dataStore.edit { it[LANGUAGE]=l } }
    suspend fun updateClockFont(f: ClockFont) { context.dataStore.edit { it[CLOCK_FONT]=f.name } }
    suspend fun updateClockColors(c: ClockCustomColors) { context.dataStore.edit { p -> p[CLOCK_USE_PRESET]=c.usePreset; p[CLOCK_CUSTOM_COLOR]=c.customColor; p[CLOCK_CUSTOM_SECONDARY]=c.customSecondaryColor }}
    // v1.0.6
    suspend fun updateVisualEffects(s: VisualEffectsSettings) { context.dataStore.edit { p -> p[FX_WATER_RIPPLE]=s.enableWaterRipple; p[FX_WAVEFORM]=s.enableWaveform; p[FX_FLUID_PARTICLES]=s.enableFluidParticles; p[FX_WAVE_AMP]=s.waveformAmplification; p[FX_RIPPLE_AMP]=s.rippleAmplification }}

    private fun ctx() = context.dataStore.data
}
