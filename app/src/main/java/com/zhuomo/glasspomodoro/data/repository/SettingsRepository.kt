package com.zhuomo.glasspomodoro.data.repository

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zhuomo.glasspomodoro.model.ClockDisplaySettings
import com.zhuomo.glasspomodoro.model.ThemeSettings
import com.zhuomo.glasspomodoro.model.WallpaperSettings
import com.zhuomo.glasspomodoro.model.WallpaperSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore 委托必须使用顶层属性（单例）
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    corruptionHandler = { corruptionException: CorruptionException ->
        // 文件损坏时删除重建，不崩溃
        throw corruptionException // 让 DataStore 自动重建文件
    }
)

class SettingsRepository(private val context: Context) {

    companion object {
        val SHOW_YEAR = booleanPreferencesKey("show_year")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_WEEKDAY = booleanPreferencesKey("show_weekday")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val USE_24HOUR = booleanPreferencesKey("use_24hour")

        val WALLPAPER_SOURCE = stringPreferencesKey("wallpaper_source")
        val LOCAL_PATH = stringPreferencesKey("local_path")
        val BING_REGION = stringPreferencesKey("bing_region")
        val BLUR_AMOUNT = floatPreferencesKey("blur_amount")

        val PRESET_INDEX = intPreferencesKey("preset_index")
        val IS_CUSTOM_COLOR = booleanPreferencesKey("is_custom_color")
        val CUSTOM_PRIMARY = longPreferencesKey("custom_primary")
        val CUSTOM_SECONDARY = longPreferencesKey("custom_secondary")
        val USE_DARK_MODE = booleanPreferencesKey("use_dark_mode")
        val AUTO_DARK_MODE = booleanPreferencesKey("auto_dark_mode")
        val DARK_START = intPreferencesKey("dark_start")
        val DARK_END = intPreferencesKey("dark_end")

        val LANGUAGE = stringPreferencesKey("language")
    }

    val clockSettings: Flow<ClockDisplaySettings> = context.dataStore.data
        .catch { e: Throwable ->
            // DataStore 读取失败不崩溃，返回默认值
            if (e is IOException || e is CorruptionException) {
                emit(emptyPreferences)
            } else throw e
        }
        .map { prefs ->
            ClockDisplaySettings(
                showYear = prefs[SHOW_YEAR] ?: false,
                showDate = prefs[SHOW_DATE] ?: true,
                showWeekday = prefs[SHOW_WEEKDAY] ?: true,
                showSeconds = prefs[SHOW_SECONDS] ?: true,
                use24Hour = prefs[USE_24HOUR] ?: true
            )
        }
        .flowOn(Dispatchers.IO)

    val wallpaperSettings: Flow<WallpaperSettings> = context.dataStore.data
        .catch { e: Throwable ->
            if (e is IOException || e is CorruptionException) {
                emit(emptyPreferences)
            } else throw e
        }
        .map { prefs ->
            WallpaperSettings(
                source = try { WallpaperSource.valueOf(prefs[WALLPAPER_SOURCE] ?: "BING") } catch (_: Exception) { WallpaperSource.BING },
                localPath = prefs[LOCAL_PATH] ?: "",
                bingRegion = prefs[BING_REGION] ?: "zh-CN",
                blurAmount = prefs[BLUR_AMOUNT] ?: 0f
            )
        }
        .flowOn(Dispatchers.IO)

    val themeSettings: Flow<ThemeSettings> = context.dataStore.data
        .catch { e: Throwable ->
            if (e is IOException || e is CorruptionException) {
                emit(emptyPreferences)
            } else throw e
        }
        .map { prefs ->
            ThemeSettings(
                presetIndex = prefs[PRESET_INDEX] ?: 0,
                isCustomColor = prefs[IS_CUSTOM_COLOR] ?: false,
                customPrimary = prefs[CUSTOM_PRIMARY] ?: 0xFF6C63FF,
                customSecondary = prefs[CUSTOM_SECONDARY] ?: 0xFF339AF0,
                useDarkMode = prefs[USE_DARK_MODE] ?: true,
                autoDarkMode = prefs[AUTO_DARK_MODE] ?: true,
                darkModeStartHour = prefs[DARK_START] ?: 19,
                darkModeEndHour = prefs[DARK_END] ?: 7
            )
        }
        .flowOn(Dispatchers.IO)

    val language: Flow<String> = context.dataStore.data
        .catch { e: Throwable ->
            if (e is IOException || e is CorruptionException) {
                emit(emptyPreferences)
            } else throw e
        }
        .map { prefs -> prefs[LANGUAGE] ?: "zh" }
        .flowOn(Dispatchers.IO)

    suspend fun updateClock(settings: ClockDisplaySettings) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_YEAR] = settings.showYear
            prefs[SHOW_DATE] = settings.showDate
            prefs[SHOW_WEEKDAY] = settings.showWeekday
            prefs[SHOW_SECONDS] = settings.showSeconds
            prefs[USE_24HOUR] = settings.use24Hour
        }
    }

    suspend fun updateWallpaper(settings: WallpaperSettings) {
        context.dataStore.edit { prefs ->
            prefs[WALLPAPER_SOURCE] = settings.source.name
            prefs[LOCAL_PATH] = settings.localPath
            prefs[BING_REGION] = settings.bingRegion
            prefs[BLUR_AMOUNT] = settings.blurAmount
        }
    }

    suspend fun updateTheme(settings: ThemeSettings) {
        context.dataStore.edit { prefs ->
            prefs[PRESET_INDEX] = settings.presetIndex
            prefs[IS_CUSTOM_COLOR] = settings.isCustomColor
            prefs[CUSTOM_PRIMARY] = settings.customPrimary
            prefs[CUSTOM_SECONDARY] = settings.customSecondary
            prefs[USE_DARK_MODE] = settings.useDarkMode
            prefs[AUTO_DARK_MODE] = settings.autoDarkMode
            prefs[DARK_START] = settings.darkModeStartHour
            prefs[DARK_END] = settings.darkModeEndHour
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }
}
