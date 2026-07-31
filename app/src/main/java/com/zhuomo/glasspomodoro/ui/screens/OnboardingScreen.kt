package com.zhuomo.glasspomodoro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuomo.glasspomodoro.data.remote.GitHubApiClient
import com.zhuomo.glasspomodoro.data.remote.GitHubUser
import com.zhuomo.glasspomodoro.data.repository.SettingsRepository
import com.zhuomo.glasspomodoro.security.TokenCryptoStore
import kotlinx.coroutines.launch

/**
 * v2.0 模块E：首次启动引导页
 *
 * 横屏沉浸式引导：
 * 1. 品牌介绍 + 功能亮点
 * 2. 可选：输入 GitHub Token（Keystore 加密存储）
 * 3. 验证 Token（GET /user）
 * 4. 可跳过（后续在设置页配置）
 */
@Composable
fun OnboardingScreen(
    repository: SettingsRepository,
    onFinished: () -> Unit,
    isZh: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenStore = remember { TokenCryptoStore(context) }
    val api = remember { GitHubApiClient() }

    var tokenInput by remember { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var verifiedUser by remember { mutableStateOf<GitHubUser?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    val preset by remember {
        mutableStateOf(
            com.zhuomo.glasspomodoro.model.ColorPresets.presets[0]
        )
    }

    // 背景光晕动画
    var glowPhase by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            glowPhase += 0.008f
            kotlinx.coroutines.delay(16L)
        }
    }
    val glowAlpha by animateFloatAsState(
        targetValue = 0.25f + 0.12f * kotlin.math.sin(glowPhase * 2f).toFloat(),
        label = "glow"
    )

    fun verify() {
        val token = tokenInput.trim()
        if (token.isEmpty()) {
            errorMsg = if (isZh) "请输入 Token" else "Please enter a token"
            return
        }
        verifying = true
        errorMsg = ""
        scope.launch {
            val user = api.verifyToken(token)
            verifying = false
            if (user != null && user.login.isNotBlank()) {
                verifiedUser = user
                tokenStore.saveToken(token)
                repository.markGitHubConfigured(user.login, user.avatarUrl)
            } else {
                errorMsg = if (isZh) "Token 无效或网络错误，请重试" else "Invalid token or network error"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2D2A5E).copy(alpha = glowAlpha),
                        Color(0xFF0D0D2B)
                    ),
                    radius = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(preset.primary, preset.secondary)))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍅", fontSize = 44.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                if (isZh) "GlassPomodoro v2.0" else "GlassPomodoro v2.0",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isZh) "物理级声学视觉引擎 · 专辑封面壁纸 · 玻璃质感沉浸体验"
                else "Acoustic Visual Engine · Album Art Wallpaper · Glass Immersion",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            // 功能亮点
            val features = if (isZh) listOf(
                "🌊 点波源物理水波 — 低频驱动波幅、中频驱动波纹密度、高频驱动粒子",
                "🎵 专辑封面动态壁纸 — 自动跟随正在播放的音乐",
                "🪟 六种玻璃遮罩 — 水波折射 / 景深模糊 / 毛玻璃…",
                "☁️ 配置云端备份 — GitHub Gist 跨设备同步"
            ) else listOf(
                "🌊 Point-source physical ripples driven by live FFT",
                "🎵 Album art as dynamic wallpaper",
                "🪟 6 glass mask styles: refraction, depth blur…",
                "☁️ Config backup via GitHub Gist"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { f ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•", color = preset.accent1, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(f, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Token 输入卡片
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x1FFFFFFF))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    if (isZh) "🔑 连接 GitHub（可选）" else "🔑 Connect GitHub (optional)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isZh) "用于配置云端备份与壁纸同步。Token 将使用 Android Keystore 加密存储，不会上传到服务器。"
                    else "Enables cloud config backup & wallpaper sync. Token is encrypted with Android Keystore.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it; errorMsg = "" },
                    placeholder = { Text("ghp_xxxxxxxxxxxxxxxx", color = Color.White.copy(alpha = 0.3f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !verifying && verifiedUser == null,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = preset.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        cursorColor = preset.primary
                    )
                )

                AnimatedVisibility(visible = errorMsg.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    Text(errorMsg, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }

                AnimatedVisibility(visible = verifiedUser != null, enter = fadeIn(), exit = fadeOut()) {
                    verifiedUser?.let { u ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text("✅", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isZh) "已连接 @${u.login}" else "Connected as @${u.login}",
                                color = Color(0xFF51CF66), fontSize = 13.sp, fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { verify() },
                        enabled = !verifying && verifiedUser == null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = preset.primary,
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        if (verifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isZh) "验证中…" else "Verifying…", fontSize = 13.sp)
                        } else {
                            Text(if (verifiedUser == null) (if (isZh) "验证并连接" else "Verify & Connect") else (if (isZh) "已连接 ✓" else "Connected ✓"), fontSize = 13.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GitHubApiClient.TOKEN_HELP_URL)))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Text(if (isZh) "获取 Token →" else "Get a token →", color = preset.accent1, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = { onFinished() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    if (isZh) "跳过，稍后配置 →" else "Skip for now →",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        }

        // 顶部玻璃高光
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.12f))
                .align(Alignment.TopCenter)
        )
    }
}
