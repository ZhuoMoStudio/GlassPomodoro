# GlassPomodoro v2.0

🍅 横屏玻璃风格番茄时钟 — 物理级声学视觉引擎 + 专辑封面动态壁纸 + 玻璃质感沉浸体验

> 从"好看的番茄时钟"升级为"综合型专注工具"：AGSL GPU 水波引擎、FFT 三频段驱动、专辑封面壁纸、GitHub 云备份。

## ✨ v2.0 特性

### 🌊 物理级声学视觉引擎（模块A）
- **点波源径向水波**：物理模型 `A(d) = A₀ × e^(-decay·d) × sin(ωt - k·d)`
- **AGSL Shader 渲染**（Android 13+ GPU 实时计算）/ **Canvas 兼容路径**（Android 7~12 同一物理模型）
- **FFT 三频段映射**：低频→波幅、中频→波纹密度、高频→粒子闪烁
- **三种显示模式**：纯水波 / 底部频谱 / 混合模式
- **波源自定义**：中心 / 左下 / 右下 / 自定义 XY 坐标
- **实时参数调节**：振幅强度、波速、衰减系数（滑块实时预览）

### 🎵 专辑封面动态壁纸（模块B）
- 通过 `MediaSessionManager` 自动获取正在播放音乐的专辑封面
- **壁纸源优先级**（可排序）：专辑封面 > Bing每日壁纸 > 本地相册 > 纯色背景
- 源切换 **Crossfade** 平滑过渡，Coil 内存缓存避免重复解码
- 专辑封面主色调提取（K-Means 聚类），可一键应用为配色

### 🪟 玻璃质感（模块C）
- **六种遮罩样式**：径向渐变 / 动态光晕 / 毛玻璃 / 科技网格 / **水波折射** / **景深模糊**
- **景深分层**：背景层 → 水波层 → 玻璃层 → 数字层（时钟文字不受扰动）
- **光影模拟**：随时间旋转的光照方向（与 AGSL Shader 联动）+ 顶部高光扫描

### 🎨 SVG 图标系统（模块D）
- 全量 `ImageVector` 矢量图标库（`Ic[模块][功能]` 命名规范）
- 番茄三态动画图标（专注/休息/暂停）：呼吸脉冲 + AnimatedContent 过渡
- v2.0 应用图标：番茄 + 声学波纹

### ☁️ GitHub 云能力（模块E）
- 首次启动引导页（可选 Token 配置，可跳过）
- **Android Keystore 加密存储** Token（AES-256-GCM，密钥不可导出）
- Token 验证（`GET /user`）、配置备份到 **Gist**（gist scope）
- 敏感配置绝不写入版本控制

### 🔧 DevOps（模块F）
- GitHub Actions：PR 自动构建（Debug）/ main 构建（Release+Artifact）/ Tag 自动发布 Release
- 签名密钥通过 GitHub Secrets 注入（`KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`）
- 最低 SDK **API 24**（desugaring 支持 java.time）、目标 SDK 35

## 🛠 技术栈

- Kotlin 100% + Jetpack Compose + Material 3
- AGSL（Android Graphics Shading Language，API 33+）
- ViewModel + StateFlow、DataStore、Room、Hilt（推荐）
- Android Visualizer API（FFT 频谱）
- Coil（壁纸加载）、OkHttp（Bing/GitHub API）
- Android Keystore System（Token 加密）

## 📱 支持的 Android 版本

| 版本 | 渲染方式 |
|------|----------|
| Android 13+ (API 33+) | AGSL RuntimeShader GPU 渲染 |
| Android 7~12 (API 24~32) | Canvas 兼容实现（同物理模型） |

## 🔑 GitHub Token 说明

Token 用于：
1. **配置备份**：将全部设置以 JSON 备份到私有 Gist（需 `gist` scope）
2. **云端能力扩展**：壁纸同步等

获取：<https://github.com/settings/tokens/new?scopes=gist&description=GlassPomodoro>

安全：Token 使用 Android Keystore（AES-256-GCM）加密存储于应用私有目录，不上传任何服务器。

## 🏗 构建与发布

```bash
# 本地构建
./gradlew assembleDebug

# 发布流程（GitHub Actions 自动完成）
git tag v2.0.0
git push origin v2.0.0
```

CI 工作流（`.github/workflows/build.yml`）：
- `pull_request` → `assembleDebug` 快速验证
- `push main` → `assembleRelease` + Artifact 上传
- `push tag v*` → `assembleRelease` + GitHub Release 发布

## 📦 开源依赖（许可证合规）

| 项目 | 用途 | 许可证 |
|------|------|--------|
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | UI 框架 | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | 矢量图标 path 数据 | Apache-2.0 |
| [Coil](https://github.com/coil-kt/coil) | 图片加载/缓存 | Apache-2.0 |
| [OkHttp](https://github.com/square/okhttp) | 网络请求 | Apache-2.0 |
| [Room](https://developer.android.com/jetpack/androidx/releases/room) | 本地数据库 | Apache-2.0 |
| [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | 设置持久化 | Apache-2.0 |
| [simple-icons GitHub](https://github.com/simple-icons/simple-icons) | GitHub 图标 path | CC0-1.0 |
| [desugar_jdk_libs](https://github.com/google/desugar_jdk_libs) | java.time desugaring | GPL-2.0 with classpath exception |
| 内置白噪音音频 | 雨/海浪/篝火/森林/溪流/白噪音 | CC0 |

## 📜 开源协议

MIT License — 详见 [LICENSE](LICENSE)
