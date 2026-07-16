# GlassPomodoro ProGuard Rules

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Kotlin
-dontwarn kotlin.**
-keep class kotlin.** { *; }

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep audio
-keep class com.zhuomo.glasspomodoro.audio.** { *; }
