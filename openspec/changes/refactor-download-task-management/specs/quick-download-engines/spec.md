## ADDED Requirements

### Requirement: 快速下载阶段按稳定 ID 解析引擎

系统 SHALL 通过唯一、稳定、可持久化的引擎 ID 从注册表解析快速下载引擎，流水线不得依赖具体引擎 action 名称。

#### Scenario: 历史快速任务

- **WHEN** 读取不含引擎字段且 `quickMode` 为 true 的历史任务
- **THEN** 系统将其解析为 Aria 引擎并保持可恢复

#### Scenario: 重复引擎 ID

- **WHEN** 注册表包含两个相同 ID 的引擎
- **THEN** 系统在初始化时拒绝该配置

### Requirement: 快速任务可切换引擎

系统 SHALL 允许停止或失败的快速任务在保持 UUID 和目标信息的情况下切换到另一个已注册引擎并重新执行。

#### Scenario: 直链任务从 Aria 切换 OkHttp

- **WHEN** Aria 直链快速任务失败且用户选择 OkHttp 直链引擎
- **THEN** 系统取消旧 runtime、更新该任务的引擎 ID，并以同一 UUID 创建新 runtime

### Requirement: 引擎输出满足后续阶段契约

快速引擎 SHALL 向后续阶段提供可读的直接媒体文件或 HLS 清单产物；已提供有效 MP4 的引擎不得被转码阶段覆盖为空路径。

#### Scenario: OkHttp 已产出 MP4

- **WHEN** OkHttp 直链引擎完成并提供有效 MP4
- **THEN** 转码阶段直接透传该文件到 Copy/NFO 阶段

### Requirement: 引擎必须声明并协商媒体能力

每个快速引擎 SHALL 声明支持的媒体类型；解析完成后系统 MUST 在启动引擎前校验媒体类型兼容性。

#### Scenario: 为 HLS 任务选择仅直链引擎

- **WHEN** HLS 快速任务选择只支持直链的 OkHttp 引擎
- **THEN** 系统不得开始写入下载产物，并进入带明确不兼容信息的可重试状态

### Requirement: 未知引擎安全降级

系统 SHALL 对历史任务中的未知引擎 ID 提供可诊断失败，不得静默使用其他引擎或因 DI 查找异常导致调度线程崩溃。

#### Scenario: 引擎已不可用

- **WHEN** 快速任务引用未注册的引擎
- **THEN** 系统进入带明确“引擎不可用”信息的可重试状态并等待用户选择
