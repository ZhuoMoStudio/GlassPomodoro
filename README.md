# 🍅 Glass Pomodoro

**横屏玻璃风格翻页时钟 + 声波番茄钟 — 专辑封面取色渐变壁纸**

[![Build](https://github.com/ZhuoMoStudio/GlassPomodoro/actions/workflows/build.yml/badge.svg)](https://github.com/ZhuoMoStudio/GlassPomodoro/actions/workflows/build.yml)

一款精美的横屏全沉浸应用，融合 **翻页时钟** 与 **番茄专注计时** 双模式，
采用 **iOS 磨砂玻璃风格**，通过 **麦克风实时检测环境音频** 驱动动态水波视觉，
并支持 **从 Spotify 等音乐应用获取专辑封面取色** 生成渐变壁纸。

---

## ✨ 功能特性

### 🕐 时钟模式
| 特性 | 说明 |
|------|------|
| 🎴 **翻页时钟** | Canvas 手绘翻牌动效，逼真机械翻页感 |
| 📅 **自定义显示** | 可独立开关：年份、日期(月/日)、星期、秒、24/12小时制 |
| 🌊 **水波涟漪** | 麦克风音频振幅驱动动态水面波纹 + 声波可视化 |
| 🖼️ **多源壁纸** | Bing 每日壁纸 / 本地相册 / 专辑封面取色 / 无 |
| 🎵 **专辑取色** | 监听 Spotify、Apple Music 等播放，提取封面主色调生成渐变 |
| 🎨 **6套主题色** | 星夜/极光/落日/樱花/薄荷/霓虹 + 自定义调色 |

### 🍅 番茄模式
| 特性 | 说明 |
|------|------|
| ⏱ **番茄计时** | 25 分钟专注 + 5 分钟短休息 + 15 分钟长休息（4轮后轮换） |
| 🔄 **自动轮换** | 专注完成自动进入休息，完成自动下一轮 |
| 📊 **专注统计** | Room 本地数据库记录每次专注 |
| 🎵 **声波背景** | 水波动效始终同步音频振幅 |

### ⚙️ 全局
| 特性 | 说明 |
|------|------|
| 🌐 **双语支持** | 中文 / English 自由切换 |
| 🌙 **暗色自动切换** | 根据时间自动切换深色/浅色模式 |
| 🎧 **白噪音框架** | 内置雨声/海浪/篝火/森林/溪流等环境音 |
| 📱 **全屏沉浸** | 强制横屏 + 隐藏系统栏 |
| 🔧 **高度自定义** | 显示项、壁纸、配色、语言均可自由配置 |

---

## 🖼️ 界面预览

```
┌──────────────────────────────────────────────┐
│          🎵 Now Playing: 晴天 - 周杰伦        │
│          🎨 专辑取色: ██ ██ ██ ██ ██ ██      │
│                                              │
│           ┌──┐ ┌──┐  :  ┌──┐ ┌──┐  :  ┌──┐  │
│           │ 2│ │ 3│  :  │ 5│ │ 9│  :  │ 4│  │
│           │  │ │  │  :  │  │ │  │  :  │  │  │
│           └──┘ └──┘     └──┘ └──┘     └──┘  │
│             08.21  星期四                      │
│                                              │
│        ～～ 水波声波 ～～                      │
│                                              │
│        [⏰ 时钟]  [🍅 专注]  [⚙️ 设置]       │
└──────────────────────────────────────────────┘
```

---

## 🛠 技术栈

| 组件 | 方案 | 许可 |
|------|------|------|
| 语言 | **Kotlin 2.0.21** | Apache 2.0 |
| UI | **Jetpack Compose + Material 3** (BOM 2026.06) | Apache 2.0 |
| 架构 | MVVM (ViewModel + StateFlow + Flow) | - |
| 数据库 | **Room** (专注统计本地存储) | Apache 2.0 |
| 偏好存储 | **DataStore Preferences** | Apache 2.0 |
| 图片加载 | **Coil** (壁纸加载) | Apache 2.0 |
| 网络 | **OkHttp** (Bing 壁纸 API) | Apache 2.0 |
| 动画 | Compose Animation API + Canvas | Apache 2.0 |
| 音频 | AudioRecord (PCM 16bit) | - |
| 媒体监听 | MediaSessionManager (系统API) | - |
| 构建 | Gradle 8.11 + AGP 8.7.3 | Apache 2.0 |
| 最低 API | 26 (Android 8.0) | - |
| 目标 API | 35 (Android 15) | - |

---

## 🚀 快速开始

```bash
# 克隆
git clone https://github.com/ZhuoMoStudio/GlassPomodoro.git
cd GlassPomodoro

# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

### 在 Android Studio 中打开
1. **File → Open** → 选择 `GlassPomodoro` 目录
2. 等待 Gradle 同步完成
3. 点击 **Run** 或按 `Shift+F10`

---

## 🎯 使用指南

### 首次启动
1. **权限请求**：麦克风权限（声波动效）/ 通知权限（提醒）
2. **默认模式**：时钟模式，显示翻页时钟 + Bing 壁纸

### 时钟模式
- 自动显示当前时间，翻牌动效
- 在设置中开关：年份、日期、星期、秒、24小时制
- 壁纸可选：Bing每日 / 本地相册 / 专辑取色 / 无
- 播放 Spotify 等音乐时，自动提取专辑封面颜色

### 专辑取色壁纸
```
设置 → 壁纸 → "🎵 当前播放专辑取色"
→ 播放 Spotify/网易云/Apple Music
→ 返回应用，自动显示专辑信息和取色结果
→ 点击喜欢的配色方案 → 自动应用为渐变壁纸
```

### 番茄模式
- 点击底部「🍅 专注」进入番茄钟
- 点击 ▶ 开始计时，⏸ 暂停，🔄 重置
- 专注完成自动轮换到休息，4轮后长休息
- 记录自动保存到本地数据库

### 设置
- 底部导航栏点击「⚙️ 设置」
- 自定义时钟显示项
- 选择壁纸来源
- 切换主题配色（6种预设）
- 切换语言（中文/English）

---

## 🧩 项目结构

```
app/src/main/java/com/zhuomo/glasspomodoro/
├── MainActivity.kt              # 入口：全屏 + 权限请求
├── model/
│   └── AppModels.kt             # 全部数据模型
├── data/
│   ├── local/FocusDatabase.kt   # Room 专注记录数据库
│   ├── remote/BingWallpaperFetcher.kt  # Bing 壁纸 API
│   └── repository/SettingsRepository.kt # DataStore 设置持久化
├── audio/
│   ├── AudioAmplitudeDetector.kt  # 麦克风振幅检测
│   └── WhiteNoisePlayer.kt        # 白噪音播放器
├── media/
│   └── MediaPlaybackMonitor.kt    # 媒体播放监听 + 专辑取色
├── viewmodel/
│   ├── MainViewModel.kt           # 全局状态管理
│   └── PomodoroViewModel.kt       # 番茄钟逻辑
└── ui/
    ├── theme/AppTheme.kt          # 主题 + 玻璃风格修饰符
    ├── components/
    │   ├── clock/FlipClock.kt     # 翻页时钟（Canvas绘制）
    │   ├── background/
    │   │   ├── WallpaperLayer.kt  # 壁纸层（Bing/本地）
    │   │   └── WaterRipple.kt     # 水波声波涟漪
    │   └── media/NowPlayingPanel.kt # 正在播放 + 取色面板
    ├── screens/
    │   ├── ClockScreen.kt         # 时钟主屏
    │   ├── PomodoroScreen.kt      # 番茄钟主屏
    │   └── SettingsScreen.kt      # 设置页
    └── navigation/
        └── AppNavigation.kt       # 底部Tab导航
```

---

## 📸 截图

所有视觉效果均使用 Compose Canvas 实时渲染，无需外部图片资源。

| 模式 | 核心视觉效果 |
|------|-------------|
| 🕐 时钟 | 翻牌数字 + 水波涟漪 + Bing/专辑壁纸 |
| 🍅 番茄 | 大号倒计时 + 声波起伏 + 专辑配色 |
| 🎵 专辑 | 封面缩略图 + 6色提取 + 渐变预览条 |
| ⚙️ 设置 | 开关项 + 颜色预设圆点 + 壁纸选择 |

---

## 📄 许可

本项目采用 **MIT License** — 详见 [LICENSE](LICENSE)

### 第三方开源组件许可
| 组件 | 许可 |
|------|------|
| Kotlin | Apache 2.0 |
| Jetpack Compose | Apache 2.0 |
| Room | Apache 2.0 |
| Coil | Apache 2.0 |
| OkHttp | Apache 2.0 |

---

## 🙏 致谢

- 翻页时钟效果灵感来自经典机械翻页钟
- 玻璃风格设计参考 iOS/macOS 设计规范
- Bing 壁纸由 [Microsoft Bing](https://www.bing.com/) 提供
- 由 [ZhuoMoStudio](https://github.com/ZhuoMoStudio) 开发维护

---

> **🍅 Glass Pomodoro** — 翻页时钟 · 声波番茄 · 专辑取色 · 玻璃美学
