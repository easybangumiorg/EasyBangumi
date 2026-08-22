## 背景

V2 播放页已经在 `EasyPlayerScaffoldBase` 前景中挂载透明 `DfmDanmakuOverlay`，并通过 `DanmakuDisplayPreferences` 即时观察总开关、类型过滤、来源过滤和时间偏移。播放器控制层仍由新旧页面共享：`VideoControl` 组合 `FullScreenVideoTopBar` 与 `EasyVideoBottomControl`，右上角“更多”目前直接设置 `showVideoScaleTypeWin = true`，画面比例以单独的右侧黑色列表呈现。

本次变更同时触及 Compose 控制层、偏好模型和 DFM 重配置。主要约束是：旧播放页没有 DFM 覆盖层，不能出现无效开关；样式调整不能 Seek ExoPlayer 或破坏已经验证的暂停/全屏同步；连续拖动调节器不能反复释放 Android View；画面比例仍需复用现有持久化和渲染逻辑。

## 目标与非目标

**目标：**

- 在普通屏和全屏底部控制条提供位置稳定、状态明确的弹幕快捷开关。
- 将右上角“更多”改成自适应 Material 3 播放设置面板，统一承载“弹幕”和“画面”。
- 支持并持久化字体大小、行高和用户语义滚动速度，同时保留总开关、类型过滤和时间偏移。
- 配置变化即时作用于当前 DFM 会话，不触发网络匹配、视频 Seek、重复播放或额外播放器实例。
- 让共享播放器控件保持通用，Legacy 页面继续使用原有控制行为。

**非目标：**

- 新增或安装外部弹幕源、发送弹幕、用户登录或弹幕社交能力。
- 在本次变更中实现关键词屏蔽、不透明度、显示区域或复杂弹幕密度算法。
- 改变 DanDanPlay 自动/手动匹配、binding、缓存和错误恢复规则。
- 重写播放器渲染层、替换 DanmakuFlameMaster 或改变现有画面比例枚举。

## 关键决策

### 1. 通过可选弹幕控制模型扩展共享播放器控件

定义播放器展示层使用的 `PlayerDanmakuControlState` 与事件集合，至少包含显示偏好、当前可用性、加载状态及打开匹配/重试入口。`CartoonPlayV2Content` 从 `DanmakuPlaybackState` 与统一显示配置派生该模型，并传给 `VideoControl`；Legacy 页面传入 `null`。

`VideoControl` 将可选模型继续传给 `EasyVideoBottomControl` 和播放设置面板。底部控制条只在模型非空时于播放/下一集之后、时间轴之前插入 48dp 触摸目标。这样共享控件不依赖 DanDanPlay 或 DFM，旧页面也不会出现无效入口。

直接在 `VideoControl` 中注入 `DanmakuDisplayPreferences` 的方案被拒绝，因为这会让所有调用方隐式获得 V2 专属能力，并把匹配状态与显示偏好分散到不同所有者。

### 2. 快捷开关只控制显示，匹配不可用时委托现有动作

已经匹配且存在弹幕时，快捷开关只写入 `displayEnabled`，DFM 从当前时间隐藏或显示；它不清除 binding、不请求网络、不替换 comments。加载中显示进度语义。未匹配、失败或弹幕源禁用时，点击委托现有手动匹配、重试或设置动作，并给出明确说明。

该决策保留 Kazumi “无数据时进入检索”的可理解行为，同时避免把来源启用、匹配成功和显示开关合并为一个布尔值。

### 3. 更多入口打开一个双分类自适应面板

将 `showScaleTypeWin` 替换为一个播放器设置面板状态，状态包含是否显示和当前分类 `Danmaku`/`Video`。面板顶部使用标题、关闭按钮和双项分段控件；分类切换只替换滚动内容。

- 横屏手机和宽屏：从右侧出现，建议宽度 320–360dp，最大不超过可用宽度的 45%。
- 竖屏紧凑宽度：使用可滚动 Material 3 Bottom Sheet，高度不超过安全区域的 90%。
- 面板外使用半透明遮罩；点击遮罩或返回只关闭面板。
- 面板打开期间保持播放器运行，由现有控制层 hold/显示机制避免控制栏自动消失。

先弹出仅有两项的菜单、再进入各自面板的方案被拒绝，因为它增加一次点击并延续两套视觉。把全部配置直接堆在一个列表的方案也被拒绝，因为画面和弹幕属于不同心智模型。

### 4. 使用统一且有边界的显示配置

在 `DanmakuDisplayPreferences` 基础上形成不可变 `DanmakuDisplayConfig`，所有入口与渲染器都消费同一份归一化配置。新增偏好使用 `Float` 并在转换层统一校验：

- `fontSizeSp`：默认 18sp，支持 12–36sp。
- `lineHeightFactor`：默认 1.2，支持 1.0–2.0；表示相邻轨道基线距离相对字体大小的倍率。
- `scrollSpeed`：默认 1.0x，支持 0.5x–2.0x；数值越大，用户感知速度越快。
- `timeOffsetMillis`：沿用现有毫秒偏好，面板以秒展示并提供小步进与归零。
- `showScroll`、`showTop`、`showBottom` 与 `enabled`：沿用现有键和默认值。

用户语义速度不得直接等同于 DFM 的原始 factor。适配器负责将 `scrollSpeed` 转换为库所需方向和安全范围，避免未来替换渲染器时泄漏第三方 API 语义。读取 `NaN`、无穷大或越界值时使用默认值或最近边界。

面板中的滑块显示当前数值；连续拖动先更新本地预览，渲染更新使用 `distinctUntilChanged` 和短时间限频，持久化在拖动结束时完成。开关、分段选择和重置操作立即持久化。

### 5. DFM 原地重配置并按影响范围重建

为 `DfmDanmakuRenderer` 增加显式显示配置入口，不让渲染器自行读取偏好。配置变化分为三类：

1. 仅可见性变化：调用显示/隐藏，不替换弹幕项目。
2. 类型或时间偏移变化：重新过滤或排程弹幕，并以 `player.currentPosition` 同步 DFM；不得调用 ExoPlayer Seek。
3. 字体大小、行高或滚动速度变化：更新 DFM context，按需要重建弹幕项目并恢复当前 DFM 时间与播放/暂停状态；不得释放播放器或创建第二个 `DanmakuView`。

字体大小在创建 `BaseDanmaku` 时由配置提供，不再硬编码 18sp。行高由适配层转换为非负轨道间距，并结合字体大小计算；实现前先通过当前 DFM 版本验证可用的 margin/line API。滚动速度只影响滚动类型，固定顶部和底部弹幕不因速度变化改变其时间语义。

每次重配置都经过 renderer policy：记录当前 view、prepared、playing 和 position 状态，合并连续配置，避免再次发生暂停恢复后从头重飘。若 DFM 某项 context 配置只能在 prepare 前设置，则仅重建 DFM context/view 内部资源并恢复当前时间，不重建 ExoPlayer。

### 6. 画面分类复用现有比例来源

“画面”分类直接消费 `CartoonPlayingViewModel.videoScaleTypeSelection` 和 `videoScaleType`，使用单选列表表达 7 个既有模式。选择后仍调用 `setVideoScaleType`，不复制枚举、不新建偏好。

旧的独立 `showScaleTypeWin` 入口在 V2 控制流程中由统一面板替代；Legacy 页面仍可保持原有侧栏，降低回归范围。若后续希望统一 Legacy，可另行变更。

### 7. 播放器内与全局设置复用内容组件

抽取无导航依赖的弹幕显示设置内容组件，播放器面板和 `PlayerSetting` 使用相同配置模型、标签、范围与重置逻辑。全局设置不强制采用播放器侧栏外壳，但不得保留一套不同的参数范围。

只有播放器面板展示当前剧集匹配摘要；全局设置只展示通用显示参数。来源启用和默认来源继续留在“弹幕源”管理页。

### 8. 页面级拥有并跨配置变化复用原生弹幕 View

`AndroidView` 的 holder 会在横竖屏配置变化时重建，即使 Activity、Compose 根节点和页面状态仍然存活。视频层通过 renderer 缓存并重新挂载同一个 `EasyTextureView` 保持连续；弹幕层必须采用相同的所有权边界，不能让 holder 每次 factory 都创建新的 `DanmakuView`。

`DfmDanmakuRenderer` 由 V2 播放页面级 `remember` 持有，并负责创建、准备和最终释放唯一的原生弹幕 View。holder 重建时 factory 从旧 parent 移除该 View 并挂入新 holder；同一 Activity Context 下不得 release、prepare 或重新装载弹幕。只有页面离开或 Activity Context 真正更换时才释放并创建新 View。

DFM `prepared()` 回调必须切回 View 主线程，并在执行前同时校验当前 View 和 `DanmakuContext` 代际，避免旧 View 的延迟回调修改新会话。holder 的销毁不得释放 renderer 资源；播放器页面所有者负责最终幂等 release。

## 风险与权衡

- [DFM 对行高没有与 Kazumi Canvas 完全一致的公开语义] → 先验证当前依赖版本的 margin/maximum-lines 能力，以用户语义倍率封装适配；若只能近似，测试实际基线间距且不向偏好层暴露库参数。
- [连续拖动字体或速度可能频繁重建 5000 条弹幕] → 本地预览与渲染更新分离，短时间限频并合并为最新配置；保留大弹幕集仪器测试。
- [配置重建可能让暂停或全屏切换再次重播弹幕] → 统一经过 renderer policy 恢复当前时间和播放状态，新增暂停中修改与全屏后恢复回归。
- [复用 View 后 DFM 仍需响应尺寸变化] → 保留同一 prepared View 和时钟，只让 DFM 重新测量显示区域；先用真机逐帧验证，若库内部尺寸重排仍产生可见空帧，再单独评估最后一帧快照过渡。
- [普通手机底部控制条加入按钮后进度条变窄] → 使用仅图标的 48dp 触摸目标，在 320dp/360dp 宽度和大字体下验证；不增加独立控制行。
- [播放器与全局设置出现双向不同步] → 两者只操作同一个 `DanmakuDisplayConfig`/偏好仓储，不在 Composable 中保存长期副本。
- [V2 与 Legacy 共用 `VideoControl` 容易误展示] → 弹幕能力参数为 nullable，默认无弹幕控件，并增加 Legacy 组合测试。

## 迁移计划

1. 为新增偏好提供兼容默认值；现有用户继续使用 18sp、1.2 行高和 1.0x 滚动速度，无需数据库迁移。
2. 先加入配置模型、归一化和 DFM 映射测试，再接入渲染器原地重配置。
3. 接入 V2 可选控制模型、快捷开关和统一设置面板；保留 Legacy 的现有参数与比例侧栏。
4. 运行单元、Compose UI 和真机 DFM 生命周期测试后启用新入口。
5. 如出现阻断问题，可让 V2 暂时回退到旧的 `showScaleTypeWin` 入口并隐藏弹幕快捷控件；新增偏好可安全保留，不影响 binding、缓存或视频历史。

## 已确认事项

- 当前依赖的 DanmakuFlameMaster 0.9.25 使用
  `marginPx = fontSizePx * (lineHeightFactor - 1)` 表达行高；基础行高不组合
  `setMaximumLines`，避免把轨道间距与显示密度耦合。
- DFM 的 `setScrollSpeedFactor` 表达滚动时长倍率，因此用户语义速度使用
  `durationFactor = 1 / scrollSpeed`。真机仪器测试确认该参数只改变滚动弹幕，
  顶部和底部固定弹幕持续时间不变。
