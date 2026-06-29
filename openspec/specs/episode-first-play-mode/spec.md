## ADDED Requirements

### Requirement: EpisodeSimple 数据结构

系统 SHALL 提供 `EpisodeSimple` 数据结构，用于剧集优先模式下的剧集表示。

#### Scenario: 创建 EpisodeSimple
- **WHEN** 源实现剧集优先模式并返回剧集列表
- **THEN** 每个剧集包含 `id`（唯一标识）、`label`（显示名称）、`order`（排序序号）、`sourceName`（来源名称，预留字段）

#### Scenario: EpisodeSimple 与 Episode 解耦
- **WHEN** 使用 EpisodeSimple
- **THEN** EpisodeSimple 不包含任何 PlayerLine 引用，与 Episode 类型完全独立

### Requirement: PlayLineSimple 数据结构

系统 SHALL 提供 `PlayLineSimple` 数据结构，用于剧集优先模式下的播放线路表示。

#### Scenario: 创建 PlayLineSimple
- **WHEN** 源实现剧集优先模式并返回播放线路列表
- **THEN** 每个播放线路包含 `id`（唯一标识）、`label`（显示名称）、`order`（排序序号）

#### Scenario: PlayLineSimple 与 PlayerLine 解耦
- **WHEN** 使用 PlayLineSimple
- **THEN** PlayLineSimple 不包含 `episodeList`，与 PlayerLine 类型完全独立

### Requirement: 模式判断接口

`PlayComponent` SHALL 提供 `isEpisodeFirstMode()` 方法，用于判断源支持的播放模式。

#### Scenario: 默认返回 false
- **WHEN** 现有源未实现 `isEpisodeFirstMode()`
- **THEN** 默认返回 `false`（线路优先模式）

#### Scenario: 新源声明剧集优先模式
- **WHEN** 新源实现 `isEpisodeFirstMode()` 并返回 `true`
- **THEN** 上层业务使用剧集优先模式的流程

### Requirement: 获取剧集列表接口

`PlayComponent` SHALL 提供 `getEpisodeList()` 方法，用于获取剧集优先模式下的剧集列表。

#### Scenario: 返回剧集列表
- **WHEN** 调用 `getEpisodeList(cartoonIndex)` 且源为剧集优先模式
- **THEN** 返回 `DataState<List<EpisodeSimple>>`，包含该卡通的所有剧集

#### Scenario: 默认返回 null
- **WHEN** 现有源未实现 `getEpisodeList()`
- **THEN** 默认返回 `null`

### Requirement: 根据剧集获取播放线路接口

`PlayComponent` SHALL 提供 `getPlayLineSimpleForEpisode()` 方法，用于根据剧集获取可用播放线路。

#### Scenario: 返回播放线路列表
- **WHEN** 调用 `getPlayLineSimpleForEpisode(cartoonIndex, episodeSimple)` 且源为剧集优先模式
- **THEN** 返回 `DataState<List<PlayLineSimple>>`，包含该剧集的所有可用播放线路

#### Scenario: 默认返回 null
- **WHEN** 现有源未实现 `getPlayLineSimpleForEpisode()`
- **THEN** 默认返回 `null`

### Requirement: 获取播放信息接口（剧集优先模式）

`PlayComponent` SHALL 提供 `getPlayInfoSimple()` 方法，用于剧集优先模式下获取播放信息。

#### Scenario: 返回播放信息
- **WHEN** 调用 `getPlayInfoSimple(cartoonIndex, playLineSimple, episodeSimple)` 且源为剧集优先模式
- **THEN** 返回 `DataState<PlayInfo>`，包含可播放的 URL 和类型

#### Scenario: 默认返回 null
- **WHEN** 现有源未实现 `getPlayInfoSimple()`
- **THEN** 默认返回 `null`

### Requirement: 缓存支持

`CacheablePlayComponentWrapper` SHALL 为 `getPlayInfoSimple()` 提供缓存支持。

#### Scenario: 缓存命中
- **WHEN** 调用 `getPlayInfoSimple()` 且缓存中存在对应 key
- **THEN** 直接返回缓存的 PlayInfo，不调用源

#### Scenario: 缓存未命中
- **WHEN** 调用 `getPlayInfoSimple()` 且缓存中不存在对应 key
- **THEN** 调用源获取 PlayInfo，写入缓存后返回

### Requirement: PlayLineIndexVM 状态扩展

`PlayLineIndexVM` SHALL 支持剧集优先模式的状态管理。

#### Scenario: 模式标记
- **WHEN** 加载播放数据时检测到剧集优先模式
- **THEN** 设置 `isEpisodeFirst = true`

#### Scenario: 剧集优先模式状态字段
- **WHEN** 处于剧集优先模式
- **THEN** VM 管理 `episodeList`、`currentEpisodeIndex`、`episodePlayLineList`、`currentEpisodePlayLine` 等状态

#### Scenario: 默认选中第一个
- **WHEN** 加载剧集列表完成
- **THEN** 默认选中第一个剧集（`currentEpisodeIndex = 0`）

#### Scenario: 切换剧集时刷新播放线路
- **WHEN** 用户选择不同剧集
- **THEN** 调用 `getPlayLineSimpleForEpisode()` 刷新播放线路列表，默认选中第一个

### Requirement: UI 组件 - Common

系统 SHALL 提供 `mediaEpisodeFirstIndex()` composable 函数，用于剧集优先模式的基础 UI 渲染。

#### Scenario: 渲染剧集列表
- **WHEN** 处于剧集优先模式
- **THEN** 显示剧集选择区域（LazyRow 横向滚动）

#### Scenario: 渲染播放线路列表
- **WHEN** 选择了剧集
- **THEN** 显示播放线路选择区域（LazyRow 横向滚动）

### Requirement: UI 组件 - Desktop

系统 SHALL 提供 `mediaEpisodeFirstIndexDesktop()` composable 函数，适配 Desktop 鼠标交互。

#### Scenario: 收起状态
- **WHEN** 默认状态
- **THEN** 使用 LazyRow 横向滚动，显示 [←][→] 箭头按钮

#### Scenario: 展开状态
- **WHEN** 点击 [展开▼] 按钮
- **THEN** 切换为 FlowRow 多行展示

#### Scenario: 更多 Dialog
- **WHEN** 点击 [更多] 按钮
- **THEN** 打开 Dialog 显示全部剧集/播放线路的网格选择

### Requirement: UI 组件 - Android

系统 SHALL 提供 `mediaEpisodeFirstIndexAndroid()` composable 函数，适配 Android 触屏交互。

#### Scenario: 横向滑动
- **WHEN** 默认状态
- **THEN** 使用 LazyRow 横向滑动选择

#### Scenario: 更多 BottomSheet
- **WHEN** 点击 [更多] 按钮
- **THEN** 打开 ModalBottomSheet 显示全部剧集/播放线路

### Requirement: 播放雷达适配

`CartoonRadarStrategyV1` SHALL 支持剧集优先模式的源验证。

#### Scenario: 剧集优先模式验证
- **WHEN** 搜索到结果且源为剧集优先模式
- **THEN** 调用 `getEpisodeList()` 验证有内容，返回 `episodes` 字段

#### Scenario: 线路优先模式验证（保持不变）
- **WHEN** 搜索到结果且源为线路优先模式
- **THEN** 调用 `getPlayLines()` 验证有内容，返回 `playerLine` 字段

### Requirement: SelectionResult 扩展

`MediaFinderVM.SelectionResult` SHALL 支持剧集优先模式的选择结果。

#### Scenario: 剧集优先模式结果
- **WHEN** 用户选择了剧集优先模式的源
- **THEN** `SelectionResult` 包含 `suggestEpisode` 字段

#### Scenario: 线路优先模式结果（保持不变）
- **WHEN** 用户选择了线路优先模式的源
- **THEN** `SelectionResult` 包含 `suggestPlayerLine` 字段
