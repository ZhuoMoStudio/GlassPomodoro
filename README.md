# GlassPomodoro v2.1.0

🍅 横屏玻璃风格番茄时钟 — 物理级声学视觉引擎 + 专辑封面动态壁纸 + 玻璃质感沉浸体验

> 从"好看的番茄时钟"升级为"综合型专注工具"：AGSL GPU 水波引擎、FFT 三频段驱动、专辑封面壁纸、玻璃拟态、开源音频合规。

## ✨ v2.1.0 新特性

### 🎧 音频可视化彻底修复（频响随声音振动）
- **Visualizer 三通道自动切换**：
  1. 附着内部白噪音 MediaPlayer 会话（100% 可靠，无需麦克风）
  2. 附着系统输出混音（捕获外部音乐 App）
  3. 麦克风 FFT 兜底（内置迭代 Cooley-Tukey FFT）
- 播放/停止白噪音时频谱源动态切换，零延迟响应

### 🎵 外部资源获取增强（专辑封面三级解析）
- Bitmap → 封面 URI（content/file）→ MediaMetadataRetriever 嵌入式封面
- 兼容 Spotify/网易云/QQ音乐/酷狗/酷我等 14+ 主流音乐 App
- 专辑封面主色自动提取并应用到水波/波形/粒子（动态取色）

### 🎨 动效设计升级
- 波形双层辉光 + 圆角频谱柱 + 柱顶光点（参考 audio-visualizer 开源设计）
- AGSL 副波源镜像涟漪（层次更丰富）
- 时钟文字玻璃投影、迷你播放卡片

## ✨ v2.0 核心特性

### 🌊 物理级声学视觉引擎
- **点波源径向水波**：`A(d) = A₀ × e^(-decay·d) × sin(ωt - k·d)`（AGSL 官方标准语法 + Canvas 兼容 + 异常自动降级）
- **FFT 三频段映射**：低频→波幅、中频→波纹密度、高频→粒子
- **三种显示模式**：纯水波 / 底部频谱 / 混合
- **波源自定义** + 振幅/波速/衰减实时调节

### 🪟 玻璃质感
- 六种遮罩：径向渐变 / 动态光晕 / 毛玻璃 / 科技网格 / 水波折射 / 景深模糊
- 景深分层渲染 + 光影旋转 + 高光扫描

### 🔗 壁纸优先级
专辑封面 > Bing每日壁纸 > 本地相册 > 纯色（可排序）

## 🛠 技术栈

Kotlin 100% + Jetpack Compose + Material 3 + AGSL（API 33+）
ViewModel + StateFlow + DataStore + Room + Coil + OkHttp
Android Visualizer API + 内置 FFT（Cooley-Tukey）

## 📱 支持版本

| 版本 | 渲染方式 |
|------|----------|
| Android 13+ (API 33+) | AGSL RuntimeShader GPU（失败自动降级） |
| Android 7~12 (API 24~32) | Canvas 兼容实现 |

## 🏗 构建与发布

```bash
./gradlew assembleDebug          # 本地
git tag v2.1.0 && git push origin v2.1.0   # 自动构建+签名+发布
```

## 📦 开源依赖与借鉴项目（许可证合规）

| 项目 | 用途 | 许可证 |
|------|------|--------|
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | UI 框架 | Apache-2.0 |
| [Material Icons](https://github.com/google/material-design-icons) | 矢量图标 | Apache-2.0 |
| [CoverArt for Android](https://github.com/android/media-samples) (Google 示例) | 专辑封面三级解析思路 | Apache-2.0 |
| [audio-visualizer-android](https://github.com/gauravk95/audio-visualizer-android) | 波形/频谱柱动效设计 | Apache-2.0 |
| [Paperize](https://github.com/rajarsheechatterjee/paperize) | 动态壁纸取色思路 | MIT |
| Coil / OkHttp / Room / DataStore | 基础组件 | Apache-2.0 |
| desugar_jdk_libs | java.time desugaring | GPL-2.0+CPE |
| 内置音频 | 6×CC0 白噪音 + 1×算法合成原创冥想音 | CC0 / Public Domain |

## 📜 开源协议

MIT License — 详见 [LICENSE](LICENSE)
