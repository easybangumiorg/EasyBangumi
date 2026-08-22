## ADDED Requirements

### Requirement: 下载任务具有单一管理边界

系统 SHALL 通过统一任务管理器执行任务入队、删除、恢复、暂停/继续和执行计划替换，UI 不得直接操作下载 runtime 或 action。

#### Scenario: 新任务入队

- **WHEN** 用户提交一个或多个下载请求
- **THEN** 系统先持久化请求，再发布 runtime 并开始调度

#### Scenario: 删除运行中任务

- **WHEN** 用户删除运行中的任务
- **THEN** 系统先取消当前 runtime 和阶段资源，再删除持久请求，晚到回调不得影响其他或替换后的任务

### Requirement: 任务恢复保持身份和执行计划

系统 SHALL 使用 UUID 作为稳定任务身份，并在普通重试时保持任务模式、快速引擎和目标集数不变。

#### Scenario: 应用重启后恢复

- **WHEN** 持久任务存在但 runtime 不存在，且用户触发重试
- **THEN** 系统使用原 UUID、模式和引擎从最后可恢复阶段继续，无法恢复时从头执行

### Requirement: 调度发布顺序确定

系统 MUST 在执行任务 action 前将对应 runtime 发布到当前任务集合，并忽略已被同 UUID 新 runtime 替换的旧状态回调。

#### Scenario: 极快阶段完成

- **WHEN** 一个阶段在 runtime 创建后立即完成
- **THEN** 状态观察者仍能看到该 runtime，且完成回调只作用于同一实例

### Requirement: 完整下载模式保持兼容

系统 SHALL 保持完整下载模式的解析、Media3 Transformer 导出和 Copy/NFO 行为及现有恢复产物约定。

#### Scenario: 执行完整下载

- **WHEN** `quickMode` 为 false 的历史或新任务开始执行
- **THEN** 系统按原完整下载阶段执行且不经过快速引擎选择
