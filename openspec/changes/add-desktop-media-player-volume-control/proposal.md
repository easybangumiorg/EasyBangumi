## Why

当前桌面端播放器缺少统一且可见的音量调节能力，用户在不同设备与播放场景下难以快速控制音量，影响观看连续性与操作体验。随着桌面端使用占比提升，需要补齐该基础能力，降低误触和调节成本。

## What Changes

- 在桌面端播放器中新增音量调节能力：控制栏常驻音量图标，悬停时显示临时 `Seekbar` 小窗并支持直接增减音量。
- 增加音量状态展示（当前音量值与静音状态），确保用户可感知。
- 增加快捷交互（如滚轮/快捷键）用于快速调节音量，并与控件行为保持一致。
- 增加音量持久化策略，在同一设备下记住上次有效音量设置。
- 统一播放器与底层桥接层的音量读写接口，保证 UI 状态与实际播放音量同步。

## Capabilities

### New Capabilities
- `desktop-player-volume-control`: 定义桌面端播放器音量调节、展示、快捷操作与持久化的行为要求。

### Modified Capabilities
- 无

## Impact

- 影响代码：`shared/src/desktopMain` 播放器 VM 与 UI 控件、`shared/foundation` `Seekbar` 复用、`shared/playcon` 桌面交互层、`libplayer/api` 与 `libplayer/vlcj` 音量接口。
- 影响系统：桌面端播放控制链路（UI -> VM -> PlayerBridge -> VLCJ）。
- 外部依赖：无新增第三方依赖，沿用现有 VLCJ 能力。
- 兼容性：仅增强桌面端能力，不改变 Android 端行为。
