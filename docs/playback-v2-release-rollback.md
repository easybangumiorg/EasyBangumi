# V2 播放页发布与回退验证记录

本文记录 V2 播放页成为默认入口前后的可验证事实、回退入口和仍需人工确认的项目。不能用本记录代替真机发布验收。

## 路由策略

- 普通播放详情入口统一使用 `DEFAULT_PLAYBACK_DETAIL_TARGET`，当前值为 `V2`，对应路由 `detailed`。
- 旧播放页保留为独立路由 `detailed_legacy`，没有修改旧页面组合本身。
- 恢复或诊断入口可调用 `navigationDetailedLegacy(id, source, enterData)`，明确进入旧播放页。
- V2 与旧版共用同一个路由参数编解码器，`id`、`source` 和完整的 `enterData` JSON 按同一规则传递。
- 当前设置页没有面向普通用户的 V2/旧版切换项。迁移期回退采用集中路由策略与显式 Legacy 导航入口，避免用户误触长期停留在旧实现。

## 2026-07-22 自动验证结果

以下项目已经由 JVM 回归测试验证：

- 未指定目标时生成 V2 路由。
- 显式指定 `Legacy` 时生成独立旧版路由。
- 包含中文、空格、斜线、问号、加号等字符的 `id`、`source`、`enterData`，在 V2 与旧版路由中均能无损还原。

验证命令：

```bash
./gradlew :app:testDebugUnitTest --tests com.heyanle.easybangumi4.navigation.PlaybackDetailRouteTest
```

执行结果：通过。

## 2026-07-22 真机验证结果

验证设备为 MEIZU 21（Android 16），使用 `feat/danmaku` 分支 Debug 构建。已完成以下检查：

- 普通番剧入口实际进入 V2 页面，番剧、播放源、当前选集与续播进度均能恢复；旧版仍保留为独立 `detailed_legacy` 路由。
- 详情折叠/展开连续动画、五个原有功能按钮、播放源横向列表、选集横向列表、全部选集面板、四种排序状态和选中态保持均正常。
- 320dp、360dp、约 411dp 三种手机宽度下检查长标题、长选集标签、多线路和长选集列表，无越界或崩溃；窄屏按设计省略文本。
- DanDanPlay 自动匹配成功加载“罪人与龙共舞”第 1 集 40 条弹幕；再次进入复用 binding/cache。手动搜索、作品选择、远端选集选择、binding 替换均成功。
- 弹幕源页只展示内置且不可移除的 DanDanPlay；默认源、显示开关、滚动/顶部/底部、来源过滤和时间偏移设置可见。“显示弹幕”关闭和恢复均即时写入，最终状态保持开启。
- 播放暂停后连续两次截取播放器区域，图像完全一致；全屏切换保持同一 07:30 时间点与弹幕位置，恢复播放后从 07:30 前进到 07:34，没有从头重飘。
- V2 的“外部播放”成功发出系统视频 Intent，并进入系统应用切换确认流程；应用内播放页面未创建第二个弹幕会话。
- DFM 真机仪器测试以 5000 条弹幕覆盖 prepare、seek、resume、pause、clear 与 release，执行通过；播放器控件触摸和 TextureView 上层显示正常。

仍属于发布流程而非本次功能实现的检查：Release 签名构建覆盖安装、各品牌设备矩阵、以及从线上版本覆盖安装后的历史数据抽样。Debug 构建已完成安装、启动与主流程回归。

## 自动化验证结果

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayV2UiTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.easybangumi4.ui.cartoon_play.DfmDanmakuRendererInstrumentedTest
```

结果：JVM 单元测试与 Debug/AndroidTest 构建通过；V2 Compose UI 4/4 通过；DFM 真机仪器测试 1/1 通过。自动化覆盖标题 0.80 边界、远端选集唯一解析、缓存/超时/stale-session、防排序丢失选中态、下一集顺序、V2/Legacy 路由参数无损编解码。

## 2026-07-25 弹幕控制与播放设置验证

本轮在 MEIZU 21（Android 16）和 `feat/danmaku` 分支 Debug 构建上完成：

- V2 普通屏与全屏底部控制条均展示 48dp 弹幕快捷开关；Legacy 的可选能力为
  `null`，不会展示无渲染能力的入口。
- GJ部第 1 集自动匹配到弹弹play 12001 条弹幕。关闭再开启快捷开关期间，
  视频从 01:17 持续推进到 02:06，未重新搜索、解除 binding 或从头播放视频；
  全屏切换后继续保持当前弹幕配置与播放时间。
- 右上角“更多”打开统一 Material 3 播放设置。横屏使用最大 360dp、至多 45%
  可用宽度的右侧面板；Compose 真机测试覆盖的紧凑竖屏使用 Bottom Sheet。
- 弹幕分类中的总开关、三种类型、12–36sp 字体、1.0–2.0 行高、
  0.5x–2.0x 滚动速度、时间偏移和确认式恢复默认均可滚动触达。
- 画面分类展示既有 7 种模式并复用 `setVideoScaleType`；真机选择“自动模式”
  后唯一选中态正确，视频、播放源、选集、binding 与播放进度保持。
- 320dp + 1.3 倍字体、360dp、约 411dp、横屏/宽屏四组
  `PlayerPlaybackSettingsUiTest` 均为 4/4 通过。矩阵测试发现并修复了窄屏大字体下
  时间校准按钮未滚入视口的问题，同时将“弹幕/画面”改为独立滚动位置。
- DFM 0.9.25 真机仪器测试在 5000 条弹幕下覆盖 12–36sp、1.0–2.0 行高、
  0.5x–2.0x 速度、暂停中重配置、Seek、全屏 View 替换、释放和控制触摸，
  2/2 通过；确认速度只影响滚动弹幕。

本轮验证命令：

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.easybangumi4.ui.cartoon_play.PlayerPlaybackSettingsUiTest,com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayV2UiTest
```

尺寸矩阵使用相同 `PlayerPlaybackSettingsUiTest` 在 320dp/1.3 倍字体、360dp、
约 411dp 和横屏条件下直接运行，完成后已恢复设备 440dpi、字体 1.0 和自动旋转。

## 回退步骤

1. 将 `DEFAULT_PLAYBACK_DETAIL_TARGET` 从 `V2` 改为 `Legacy`，重新构建发布包；无需修改各业务入口。
2. 保留 `detailed` 与 `detailed_legacy` 两个目的地，不删除 V2 数据或弹幕缓存，避免产生不可逆迁移。
3. 执行上述 JVM 路由测试，并按“2026-07-22 真机验证结果”重跑旧版播放主路径。
4. 在发布记录中写明实际构建版本、设备、入口、播放源与验证结果；只有全部必需项目通过后，才能声明回退验证完成。

若只回退本轮播放器控制 UI 而保留已有弹幕数据，可在
`CartoonPlayV2Content` 停止向共享 `VideoControl` 注入
`danmakuControlState` 和 `onShowPlayerSettings`，并移除 V2 的
`AdaptivePlayerSettingsPanel` 挂载。共享控件的默认参数会隐藏快捷开关，并让
右上角“更多”重新使用原 `showVideoScaleTypeWin` 侧栏；binding、缓存和新增显示偏好
均无需删除。
