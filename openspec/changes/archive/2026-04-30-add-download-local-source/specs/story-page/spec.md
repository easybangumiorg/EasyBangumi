## ADDED Requirements

### Requirement: Story 页面入口

系统 SHALL 在底部导航栏提供 Story 页面入口，包含三个子 Tab：下载中、已完成、本地番剧。

#### Scenario: 显示 Story Tab
- **WHEN** 应用主界面加载
- **THEN** 底部导航栏显示 Story Tab

#### Scenario: 切换子 Tab
- **WHEN** 用户点击不同的子 Tab
- **THEN** 显示对应的内容列表

### Requirement: 下载中列表

Story 页面 SHALL 显示所有正在进行的下载任务，包含进度、状态和操作按钮。

#### Scenario: 显示下载进度
- **WHEN** 有正在进行的下载任务
- **THEN** 列表项显示番剧名称、剧集、下载进度百分比、当前状态文本

#### Scenario: 暂停下载
- **WHEN** 用户点击暂停按钮
- **THEN** 对应任务暂停，状态变为"已暂停"

#### Scenario: 恢复下载
- **WHEN** 用户点击恢复按钮
- **THEN** 对应任务从暂停处继续下载

#### Scenario: 取消下载
- **WHEN** 用户点击取消按钮并确认
- **THEN** 对应任务取消，清理缓存文件，删除持久化请求

### Requirement: 已完成列表

Story 页面 SHALL 显示所有已下载完成的番剧。

#### Scenario: 显示已完成番剧
- **WHEN** 存在已下载完成的番剧
- **THEN** 列表显示番剧封面、名称、已下载集数

#### Scenario: 跳转到本地番剧详情
- **WHEN** 用户点击已完成的番剧
- **THEN** 跳转到对应的本地番剧详情页

### Requirement: 本地番剧列表

Story 页面 SHALL 显示所有本地扫描到的番剧。

#### Scenario: 显示本地番剧
- **WHEN** 本地源扫描到番剧
- **THEN** 列表以卡片形式显示番剧封面、名称、标签

#### Scenario: 播放本地番剧
- **WHEN** 用户点击本地番剧并选择剧集
- **THEN** 使用本地源的 PlayComponent 获取播放地址并开始播放

#### Scenario: 删除本地番剧
- **WHEN** 用户长按本地番剧并选择删除
- **THEN** 弹出确认对话框，确认后删除文件夹并刷新列表

### Requirement: 发起下载

用户 SHALL 能从在线源的详情页选择剧集并发起下载。

#### Scenario: 选择剧集下载
- **WHEN** 用户在在线源详情页点击下载按钮并选择剧集
- **THEN** 系统创建下载请求，任务出现在"下载中"列表

#### Scenario: 选择目标本地番剧
- **WHEN** 用户发起下载
- **THEN** 系统自动匹配或让用户选择目标本地番剧条目

#### Scenario: 无目标时自动创建
- **WHEN** 目标本地番剧不存在
- **THEN** 系统自动创建本地番剧文件夹和 tvshow.nfo

### Requirement: StoryController 协调

StoryController SHALL 协调下载系统和本地源，提供统一的数据流。

#### Scenario: 下载完成通知刷新
- **WHEN** 下载任务完成
- **THEN** StoryController 触发 LocalCartoonController.refresh()

#### Scenario: 统一数据视图
- **WHEN** UI 请求数据
- **THEN** StoryController 提供 downloadItems（Flow）和 localItems（Flow）
