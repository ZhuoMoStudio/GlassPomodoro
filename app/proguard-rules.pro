# ===========================================
# GlassPomodoro ProGuard / R8 保留规则
# ===========================================
# 编译时保留这些类，防止 R8 误删导致运行时崩溃

# ===== AndroidX / Jetpack =====
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ===== Jetpack Compose (必须完整保留) =====
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===== Kotlin =====
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-dontwarn kotlinx.coroutines.**

# ===== OkHttp =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ===== Coil (图片加载) =====
-keep class coil.** { *; }
-dontwarn coil.**

# ===== Room =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# ===== DataStore =====
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ===== Lifecycle / ViewModel =====
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ===== Navigation =====
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ===== 应用自身代码 =====
-keep class com.zhuomo.glasspomodoro.** { *; }
-keepclassmembers class com.zhuomo.glasspomodoro.** { *; }

# ===== Kotlin 反射 =====
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**
