# 🍅 Glass Pomodoro

**横屏玻璃风格番茄时钟 — 声波动效驱动**

[![Build](https://github.com/ZhuoMoStudio/GlassPomodoro/actions/workflows/build.yml/badge.svg)](https://github.com/ZhuoMoStudio/GlassPomodoro/actions/workflows/build.yml)

一款精美的横屏番茄钟应用，采用 **iOS 磨砂玻璃风格**，通过 **麦克风实时检测环境音频** 驱动动态视觉效果。

---

## ✨ 功能特性

| 特性 | 说明 |
|------|------|
| ⏱ **番茄计时** | 25 分钟专注 + 5 分钟短休息 + 15 分钟长休息（4 轮后） |
| 🎵 **声波动效** | 麦克风检测环境音频振幅，驱动三层动态波浪和漂浮粒子 |
| 🪟 **玻璃风格** | iOS 磨砂玻璃（Glassmorphism）UI，半透明卡片 + 模糊背景 |
| 🌌 **动态背景** | 缓慢漂移的彩色光晕 + 闪烁繁星 + 音频响应呼吸光晕 |
| 📱 **横屏沉浸** | 强制横屏 + 隐藏系统栏，全沉浸式体验 |
| 🔄 **自动轮换** | 专注完成自动进入休息，休息完成自动进入下一轮专注 |
| 🎯 **轮次统计** | 实时显示已完成专注轮数 |

---

## 📸 界面预览

```
┌──────────────────────────────────────────────┐
│           🍅 Glass Pomodoro                   │
│           已完成 0 轮专注                      │
│                                              │
│     [专注 25分]  [短休息 5分]  [长休息 15分]    │
│                                              │
│           ╭─────────────╮                    │
│           │   25:00     │                    │
│           │   专注       │                    │
│           ╰─────────────╯                    │
│                                              │
│          [⏪]    [▶/⏸]    [⏩]              │
│                                              │
│           🎯 专注于当前任务                    │
│        ～～  ～～  ～～  ～～                  │
│         (声波动效可视化)                       │
└──────────────────────────────────────────────┘
```

---

## 🛠 技术栈

| 组件 | 方案 |
|------|------|
| 语言 | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM (ViewModel + StateFlow) |
| 动画 | Compose Animation API + Canvas |
| 音频 | AudioRecord (PCM 16bit, 44100Hz) |
| 构建 | Gradle 8.11 + AGP 8.7.3 |
| 最低 API | 26 (Android 8.0) |
| 目标 API | 35 (Android 15) |

---

## 🚀 快速开始

### 克隆

```bash
git clone https://github.com/ZhuoMoStudio/GlassPomodoro.git
cd GlassPomodoro
```

### 构建

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

### 在 Android Studio 中打开

1. 打开 Android Studio → **File → Open**
2. 选择 `GlassPomodoro` 目录
3. 等待 Gradle 同步完成
4. 点击 **Run** 运行

---

## 🎯 使用说明

1. **权限请求**：首次启动会请求麦克风权限，用于声波动效
2. **选择会话**：点击顶部「专注/短休息/长休息」切换模式
3. **开始计时**：点击绿色播放按钮开始
4. **查看动效**：说话或播放音乐，观察声波和粒子反应
5. **自动切换**：专注结束后自动进入休息，反之亦然

---

## 🧩 项目结构

```
app/src/main/java/com/zhuomo/glasspomodoro/
├── MainActivity.kt              # 入口 Activity
├── audio/
│   └── AudioAmplitudeDetector.kt # 音频振幅检测
├── model/
│   └── PomodoroState.kt          # 状态模型
├── viewmodel/
│   └── PomodoroViewModel.kt      # 业务逻辑
└── ui/
    ├── theme/
    │   ├── Color.kt              # 玻璃调色板
    │   └── GlassStyle.kt         # 磨砂玻璃修饰符 + 主题
    ├── components/
    │   ├── BackgroundEffect.kt    # 动态渐变背景
    │   ├── GlassCard.kt          # 可复用玻璃卡片
    │   ├── TimerDisplay.kt       # 环形进度 + 大号时间
    │   ├── AudioVisualizer.kt    # 声波动效可视化
    │   ├── ControlButtons.kt     # 开始/暂停/重置
    │   └── SessionSelector.kt    # 会话类型切换
    └── screens/
        └── PomodoroScreen.kt     # 主屏幕布局
```

---

## 🤝 贡献

欢迎 PR！请确保代码风格一致，并通过现有测试。

---

## 📄 许可

MIT License — 详见 [LICENSE](LICENSE)

---

## 🙏 致谢

- 灵感来自 [Pomodoro Technique](https://en.wikipedia.org/wiki/Pomodoro_Technique)
- 玻璃风格设计参考 iOS/macOS 设计规范
- 由 [ZhuoMoStudio](https://github.com/ZhuoMoStudio) 开发维护
