## Why

当前下载实现把任务持久化、运行时调度、具体 Aria 引擎和 Compose 页面通过可变运行时对象耦合在一起：快速模式的 action 链写死具体引擎，任务重试与删除存在竞态，下载列表也无法稳定表达任务身份、阶段和引擎。需要在不破坏现有完整下载模式和历史任务恢复能力的前提下，建立可扩展、可观测且可测试的下载任务架构。

## What Changes

- 引入统一的下载任务管理边界，集中处理入队、恢复、暂停/继续、删除和模式/引擎切换。
- 保持完整下载模式的 `Parse → Transformer → Copy/NFO` 行为不变。
- 将快速下载模式的下载阶段改为引擎注册表与稳定引擎 ID 驱动；旧任务默认映射到 Aria。
- 注册 Aria 快速引擎与支持 HTTP Range 的 OkHttp 直链引擎，使兼容任务可以保留任务身份并切换引擎重试，也可以显式回退完整模式。
- 修复调度器中“先执行、后发布 runtime”的竞态，并以稳定任务 ID 驱动列表选择和批量操作。
- 重构下载列表页面，展示模式、引擎、阶段、状态和进度，提供明确的重试/切换引擎/删除操作、空状态与批量操作。
- 补充任务计划、引擎解析、状态投影和列表选择的单元测试。

## Capabilities

### New Capabilities

- `download-task-management`: 下载任务的持久化、调度、生命周期操作、稳定状态投影和恢复语义。
- `quick-download-engines`: 快速下载阶段的引擎注册、选择、切换、兼容性与旧任务迁移。
- `download-list-experience`: 基于稳定任务身份的下载列表、状态展示、单任务操作和批量管理。

### Modified Capabilities

<!-- 当前 openspec/specs 中没有既有下载能力规格。 -->

## Impact

- 影响 `cartoon/story/download` 下的 request、runtime、dispatcher、action 与依赖注入注册。
- 影响 `CartoonStoryController` 下载管理 API 及 `ui/story/download` 页面和 ViewModel。
- `CartoonDownloadReq` 新增带默认值的引擎字段，历史 JSON 无需数据迁移即可继续读取。
- 不移除 Aria、Media3 Transformer 或现有完整下载链路，也不改变已下载本地文件格式。
