package com.zhuomo.glasspomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.zhuomo.glasspomodoro.ui.navigation.AppNavigation
import com.zhuomo.glasspomodoro.ui.theme.GlassPomodoroTheme
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import com.zhuomo.glasspomodoro.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求麦克风权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            // 先设置 Compose 内容，确保 window 已就绪
            val mainVM: MainViewModel = viewModel()
            val preset = currentColorPreset(mainVM.settingsRepo)
            GlassPomodoroTheme(preset = preset, darkTheme = true) {
                AppNavigation(mainViewModel = mainVM)
            }
        }

        // Window 内容就绪后再设置全屏（避免某些设备上崩溃）
        window.decorView.post {
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } catch (_: Exception) {
                // 全屏模式失败不影响核心功能
            }
        }
    }
}
