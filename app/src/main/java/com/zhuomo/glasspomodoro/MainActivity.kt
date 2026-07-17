package com.zhuomo.glasspomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.zhuomo.glasspomodoro.model.ThemeSettings
import com.zhuomo.glasspomodoro.ui.navigation.AppNavigation
import com.zhuomo.glasspomodoro.ui.theme.GlassPomodoroTheme
import com.zhuomo.glasspomodoro.ui.theme.currentColorPreset
import com.zhuomo.glasspomodoro.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainVM: MainViewModel = viewModel()
            val preset = currentColorPreset(mainVM.settingsRepo)
            val theme by mainVM.settingsRepo.themeSettings.collectAsState(initial = ThemeSettings())
            GlassPomodoroTheme(preset = preset, themeMode = theme.themeMode) {
                AppNavigation(mainViewModel = mainVM)
            }
        }
        window.decorView.post {
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).apply { hide(WindowInsetsCompat.Type.systemBars()); systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
            } catch (_: Exception) {}
        }
        window.decorView.postDelayed({
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                try { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) } catch (_: Exception) {}
        }, 500)
    }
}
