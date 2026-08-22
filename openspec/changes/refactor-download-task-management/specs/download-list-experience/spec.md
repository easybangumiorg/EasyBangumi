## ADDED Requirements

### Requirement: 列表使用稳定任务身份

下载列表 SHALL 使用任务 UUID 作为 LazyColumn key、选择身份和操作参数，不得使用包含 runtime 引用的对象相等性。

#### Scenario: 任务恢复替换 runtime

- **WHEN** 已选择任务的 runtime 被恢复流程替换
- **THEN** 该任务仍保持选择状态且批量操作作用于最新请求

### Requirement: 列表展示可诊断状态

每个下载任务 SHALL 展示番剧、集数、完整/快速模式、快速引擎、当前阶段、状态和可用进度信息。

#### Scenario: 快速任务失败

- **WHEN** 快速任务进入失败状态
- **THEN** 列表展示错误状态、当前引擎，并提供重试、切换引擎和回退完整模式入口

### Requirement: 列表提供完整集合状态

下载列表 SHALL 为加载、空集合、正常集合和读取失败提供明确且互斥的页面状态。

#### Scenario: 没有下载任务

- **WHEN** 持久任务集合为空且加载完成
- **THEN** 页面展示下载任务空状态而不是空白列表

### Requirement: 批量操作作用于最新任务集合

列表 SHALL 支持按稳定 ID 全选、清除选择和批量删除；已从最新集合移除的 ID 必须自动从选择中清理。

#### Scenario: 选中任务同时完成

- **WHEN** 选中的任务在批量操作前完成并从集合移除
- **THEN** ViewModel 清理该 ID，后续操作不得删除或修改其他任务
