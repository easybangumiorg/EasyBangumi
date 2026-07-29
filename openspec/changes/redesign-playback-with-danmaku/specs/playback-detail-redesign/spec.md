## ADDED Requirements

### Requirement: Isolated playback-detail page preserves the legacy page
The system SHALL provide a new playback-detail page for the redesigned experience without modifying the existing playback-detail page's composition or behavior. The system SHALL provide a scoped mechanism to select the legacy page during the migration.

#### Scenario: Default V2 playback entry
- **WHEN** a user opens a supported cartoon playback detail through the V2 route
- **THEN** the system SHALL show the redesigned playback-detail page using the existing playback and cartoon state

#### Scenario: Legacy rollback entry
- **WHEN** the legacy playback-detail selection mechanism is active
- **THEN** the system SHALL open the unchanged legacy playback-detail page for the same cartoon and selected episode

### Requirement: V2 page preserves core media information and actions
The V2 page SHALL display the video player, cartoon identity, a synopsis clamped to two lines, an expand affordance, and the existing actions for follow, search, website, download, and external playback. The action row SHALL have a visible divider below the entire group and SHALL NOT place dividers between individual actions.

#### Scenario: Collapsed synopsis
- **WHEN** the V2 page first displays a cartoon with a synopsis
- **THEN** the synopsis SHALL be limited to two lines and an expand affordance SHALL be visible

#### Scenario: Expanded synopsis
- **WHEN** the user activates the synopsis expand affordance
- **THEN** the page SHALL display the full synopsis without removing the core action row

### Requirement: Playback source and quick episode navigation are grouped
The V2 page SHALL present playback source selection as a horizontally scrollable source row and SHALL present the selected source's episodes as a horizontally scrollable quick episode rail. The quick episode buttons SHALL use a wider short rounded shape and SHALL indicate the currently playing episode.

#### Scenario: Switch playback source
- **WHEN** the user selects another playback source
- **THEN** the quick episode rail SHALL update to that source's sorted episodes without changing the current playing episode until a new episode is selected

#### Scenario: Select an episode from the quick rail
- **WHEN** the user selects an episode in the quick episode rail
- **THEN** the system SHALL change playback using the selected source and episode

### Requirement: Episode controls and picker support complete selection
The episode heading SHALL expose sorting and an all-episodes action. The all-episodes action SHALL open a Material bottom sheet with source selection, episode search, current sorting, a grid of episodes, and current-episode focus.

#### Scenario: Open all episodes
- **WHEN** the user activates all episodes from the episode heading
- **THEN** the system SHALL open the episode picker bottom sheet without inserting an all-episodes item into the quick episode rail

#### Scenario: Select an episode in the picker
- **WHEN** the user selects an episode in the episode picker
- **THEN** the system SHALL update playback and dismiss the picker

#### Scenario: Apply episode sorting
- **WHEN** the user changes the episode sort or display configuration
- **THEN** the quick rail and episode picker SHALL use the same updated ordering configuration

### Requirement: 详情展开交互具有连续动画
V2 页面 SHALL 让整个媒体详情区域承担展开/收起点击热区，SHALL 使用上下箭头表达状态，并 SHALL 使用与旧版一致的顺序淡出、淡入动画切换折叠摘要与完整详情。

#### Scenario: 展开完整详情
- **WHEN** 用户点击折叠的媒体详情区域
- **THEN** 页面 SHALL 先淡出折叠内容、再淡入完整详情，并将箭头切换为收起状态

#### Scenario: 收起完整详情
- **WHEN** 用户点击已展开的媒体详情区域
- **THEN** 页面 SHALL 以对应动画恢复两行摘要，并将箭头切换为展开状态

### Requirement: 排序使用旧版语义且保持播放身份
V2 的排序入口 SHALL 打开 Material Bottom Sheet 中的排序列表，支持“默认”和“名称”维度及其正序/倒序状态。排序 SHALL 只改变快捷选集和全部选集的展示顺序，SHALL NOT 改变当前浏览线路、当前播放线路、当前播放选集或播放器进度。

#### Scenario: 切换排序维度
- **WHEN** 用户从排序 Bottom Sheet 选择“默认”或“名称”并切换方向
- **THEN** 快捷选集与全部选集 SHALL 使用相同的新顺序，播放源和当前选集高亮 SHALL 保持不变

#### Scenario: 详情数据刷新后恢复选中态
- **WHEN** 排序持久化导致 `CartoonInfo` 和 `PlayLineWrapper` 重新创建
- **THEN** V2 SHALL 依据 `playLineId` 与 `episodeId` 恢复浏览和播放选中态，而不是比较 wrapper 实例

### Requirement: 播放器与详情面板之间显示进度分割线
V2 页面 SHALL 在播放器与详情面板之间显示主题色矩形分割线；当播放时长有效时，分割线 SHALL 使用当前播放进度展示已播放比例。

#### Scenario: 播放进度更新
- **WHEN** `ControlViewModel.position` 或 `during` 更新且时长有效
- **THEN** 分割线 SHALL 平滑更新主题色已播放部分的宽度

#### Scenario: 播放时长无效
- **WHEN** 播放时长为零、未知或无效
- **THEN** 分割线 SHALL 保留轨道但显示零进度

### Requirement: 弹幕区域遵循统一面板视觉
弹幕标题与卡片 SHALL 与播放源和选集共享相同水平边距；手动匹配结果 SHALL 继承 Bottom Sheet 容器颜色，并在深浅主题下保持一致。

匹配成功卡片的副标题 SHALL 使用结构化双行：来源、选集和缓存信息位于第一行，并在空间不足时以省略号截断；`N 条弹幕` 位于独立第二行且不附加装饰性省略号，不得依赖整段文本的自然换行。

#### Scenario: 显示手动匹配结果
- **WHEN** DanDanPlay 搜索返回作品或选集列表
- **THEN** 每个结果项 SHALL 使用透明列表容器继承 Bottom Sheet 背景，同时保留可点击反馈
