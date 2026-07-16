package com.zhuomo.glasspomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
    ) { granted ->
        // 权限结果由 ViewModel 自动处理
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 先设置 Compose 内容，确保窗口已完全初始化
        setContent {
            val mainVM: MainViewModel = viewModel()
            val preset = currentColorPreset(mainVM.settingsRepo)
            GlassPomodoroTheme(preset = preset, darkTheme = true) {
                AppNavigation(mainViewModel = mainVM)
            }
        }

        // 全屏沉浸模式（在 Content 设置之后执行，避免闪退）
        window.decorView.post {
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } catch (_: Exception) {
                // 全屏并非必需功能，失败不影响使用
            }
        }

        // 延迟请求麦克风权限（避免启动时权限弹窗干扰首帧渲染）
        window.decorView.postDelayed({
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } catch (_: Exception) {
                    // 权限请求失败不影响核心功能
                }
            }
        }, 500)
    }
}
