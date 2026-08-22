## Context

现有 `CartoonDownloadReq.quickMode` 在实体内部直接选择具体 action 名称；快速模式固定使用 `AriaAction`，完整模式固定使用 `TransformerAction`。`CartoonStoryControllerImpl` 分别调用持久化控制器和调度器，无法保证操作顺序；调度器创建任务时先执行 runnable 再写入 `runtimeMap`。下载页持有 `CartoonDownloadInfo` 对象作为选择身份，并直接调用 runtime/currentAction，导致 runtime 替换后选择失效且 UI 越过任务管理边界。

约束包括：历史 JSON 必须可读；完整下载模式必须原样保留；Aria 的断点数据和现有转码产物必须继续可恢复；不能改动当前工作区内无关的播放器与品牌资源修改。

## Goals / Non-Goals

**Goals:**

- 建立单一任务管理入口和确定的操作顺序。
- 让快速下载计划依赖稳定引擎 ID，而非具体 action 类名。
- 提供至少两种可选执行路径，并允许失败/停止任务切换后重试。
- 用稳定 task ID 管理 UI 选择，明确显示任务模式、引擎、阶段和可用操作。
- 保持历史任务、Aria 缓存、完整模式和最终本地文件行为兼容。

**Non-Goals:**

- 在本次变更中替换 Aria 的数据库或重写完整 HLS 下载协议栈。
- 改变本地番剧目录、NFO 格式或已完成条目的数据库结构。
- 支持任务优先级、定时下载、跨设备同步或操作系统级 DownloadManager。

## Decisions

### 1. 任务计划与具体引擎分离

`CartoonDownloadReq` 保留 `quickMode` 以兼容历史 JSON，并新增默认值为 `aria` 的 `quickDownloadEngineId`。完整模式仍解析为原有 action 链；快速模式解析为 `Parse → QuickDownloadAction → Transcode → Copy/NFO`。

`QuickDownloadAction` 是稳定的流水线阶段，它通过 `QuickDownloadEngineRegistry` 按任务字段解析具体引擎。引擎注册表校验 ID 唯一性；历史缺失字段由 DTO 默认值映射到 Aria，明确但未知的 ID 则进入可诊断失败，避免误用不兼容检查点。相比把引擎 ID 编码进 action 名称，此方案不会污染流水线，也不会让恢复逻辑依赖 DI key 拼接规则。

### 2. 默认 Aria 与受能力约束的 OkHttp 直链引擎

Aria 继续作为默认快速引擎，保留现有直链/HLS、断点恢复和缓存格式。另注册独立的 OkHttp 直链引擎，支持请求头、HTTP Range 检查点、暂停和继续，并明确声明只支持直链媒体。解析完成后 `QuickDownloadAction` 必须按媒体类型与引擎能力协商；不兼容时进入可诊断失败状态，不能尝试用不完整的 HLS 实现下载。

Media3 Transformer 只属于完整模式，不注册为快速下载引擎。快速引擎统一返回 `DirectFile` 或 `HlsBundle` 中立产物，转码阶段不再读取 Aria 类型；这确保以后可注册完整实现的其他 HLS 引擎。

### 3. 单一任务管理入口

新增 `CartoonDownloadTaskManager`，成为 controller/UI 的命令边界：

- 入队：先持久化，后发布并启动 runtime；
- 删除/替换：先停止旧 runtime，再更新持久任务；
- 普通重试：保持模式和引擎，调用可恢复调度；
- 切换引擎：仅允许快速任务，停止旧 runtime、原位替换请求并启动新 runtime；
- 回退完整模式：保留 UUID 和目标集数，切换模式后重新执行。

保持 UUID 可避免列表闪烁和重复任务；停止旧 runtime 后再替换可避免旧回调删除新请求。

### 4. 调度先发布、后执行

调度器先构造全部 runtime，原子写入 runtime map，再向单线程调度器提交 runnable。状态回调只处理 map 中仍与该 UUID 对应的 runtime，防止被替换的旧回调影响新任务。

这是对当前“runnable 可能在 map 发布前完成”的修复，也为后续任务快照流预留稳定边界。

### 5. UI 使用稳定 ID 和投影模型

`DownloadViewModel` 的 selection 改为 `Set<String>`，长按区间、全选、删除和重试均在事件处理时按 UUID 解析最新任务。列表使用 `key = uuid`。UI 不再直接调用 `currentAction.toggle`，而是通过 controller/manager 触发操作。

任务卡展示：

- 番剧与集数；
- 完整/快速模式；
- 快速引擎显示名；
- 停止、等待、执行、失败状态；
- 当前 action 对应的用户可读阶段；
- 进度与速度/子状态；
- 明确的点击语义和失败任务引擎选择弹窗。

### 6. 兼容性优先的数据迁移

新增字段必须有构造默认值。旧 `quickMode=true` 任务等价于 `engineId=aria`；旧 `quickMode=false` 任务忽略引擎字段。未知或已卸载引擎在展示时标记不可用，执行时进入明确错误并允许用户重新选择。

不批量重写 JSON；任务在用户切换引擎时才原位更新，从而减少升级风险。

## Risks / Trade-offs

- [OkHttp 引擎不支持 HLS] → 在 descriptor 与 UI 中明确标记“仅直链”，运行前执行能力协商并保留 Aria 作为 HLS 默认引擎。
- [旧 Aria 回调在引擎切换后晚到] → 调度器和 action 回调校验当前 runtime 身份，切换时先取消旧 runtime。
- [持久化文件和 runtime 不是数据库事务] → 所有命令在管理器内固定顺序且幂等；应用重启时以持久任务为事实来源并允许恢复。
- [Compose 进度仍来自现有 DownloadingBus] → 本次保留这一经过验证的高频通道，任务身份与命令已解除 runtime 耦合；后续可独立迁移为 Flow 快照。

## Migration Plan

1. 为请求添加带默认值的引擎 ID，并引入任务计划/引擎注册表测试。
2. 注册 `QuickDownloadAction`、Aria 和 OkHttp 直链引擎，保持完整模式链不变。
3. 引入任务管理器并将 StoryController 命令迁移到该边界。
4. 调整调度器发布顺序和陈旧 runtime 保护。
5. 迁移下载列表到 task ID 选择与引擎切换对话框。
6. 运行 OpenSpec 校验、单元测试、Debug 编译；失败时可回滚新 action/manager，历史请求仍可由默认字段读取。

## Open Questions

- 后续是否将独立 OkHttp/Media3 Offline HLS 下载器作为第三个“无转码快速引擎”注册。
- 是否在设置页提供新任务默认快速引擎；本次仅在任务失败/停止后按任务切换。
