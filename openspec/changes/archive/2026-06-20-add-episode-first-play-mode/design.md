## Context

当前 EasyBangumi 的播放组件架构采用"线路优先"模式：

```
CartoonIndex → getPlayLines() → List<PlayerLine>
                                      │
                                      └── episodeList: List<Episode>
                                      │
                                      ▼
getPlayInfo(cartoonIndex, playerLine, episode) → PlayInfo
```

这种模式下，`PlayerLine` 包含 `episodeList`，形成嵌套关系。部分番剧源的组织方式相反——先有独立的剧集列表，同一剧集可从不同播放线路获取。现有架构无法优雅支持这类源。

**约束条件**：
- 现有源无需改动，必须向后兼容
- 两种模式并存，由源自行选择
- Desktop 和 Android 有不同的交互模式

## Goals / Non-Goals

**Goals:**
- 新增剧集优先模式，与线路优先模式并存
- 保持接口向后兼容，现有源零改动
- 提供清晰的模式判断机制
- Desktop 和 Android 各有适配的交互体验

**Non-Goals:**
- 不改造现有源
- 不废弃线路优先模式
- 不统一两种模式的数据结构（保持解耦）

## Decisions

### 决策 1：扩展接口 vs 新接口

**选择**：扩展现有 `IPlayComponent` 接口，新增可选方法

**理由**：
- 一个接口，类型判断简单（调用 `isEpisodeFirstMode()`）
- 现有 `ComponentBusiness<PlayComponent>` 等包装器无需修改泛型
- 默认返回 `null`/`false`，现有源零改动

**替代方案**：
- 新增独立接口 `EpisodeFirstPlayComponent`：需要类型检查，包装器需适配

### 决策 2：数据结构命名

**选择**：新增 `EpisodeSimple`、`PlayLineSimple`，与原结构解耦

**理由**：
- 命名明确区分两种模式的数据结构
- 无嵌套关系（`PlayLineSimple` 不包含 `episodeList`）
- `EpisodeSimple` 新增 `sourceName` 预留字段

**替代方案**：
- 复用 `Episode`：字段语义可能不同，扩展困难

### 决策 3：PlayInfo 复用

**选择**：剧集优先模式的 `getPlayInfoSimple()` 返回 `PlayInfo`（复用现有结构）

**理由**：
- PlayInfo 结构两种模式完全一致（url + type + header）
- 避免不必要的类型转换

### 决策 4：ViewModel 状态管理

**选择**：在现有 `PlayLineIndexVM` 中扩展，通过 `isEpisodeFirst` 标记区分模式

**理由**：
- 共享同一个 ViewModel 实例，状态管理集中
- UI 层根据 `isEpisodeFirst` 选择渲染组件
- 避免创建新的 ViewModel 类增加复杂度

**替代方案**：
- 新增 `EpisodeFirstPlayLineIndexVM`：需要在上层切换 VM 实例

### 决策 5：UI 交互模式

**Desktop**：
- 收起状态：LazyRow 横向滚动 + [←][→] 箭头按钮
- 展开状态：FlowRow 多行展示
- [更多] 按钮：打开 Dialog 网格选择

**Android**：
- LazyRow 横向滑动
- [更多] 按钮：打开 ModalBottomSheet

### 决策 6：播放雷达适配

**选择**：在 `CartoonRadarStrategyV1` 中先调用 `isEpisodeFirstMode()` 判断模式，再调用对应验证方法

**理由**：
- 雷达搜索需要验证源有内容
- 线路优先：验证 `getPlayLines()` 有结果
- 剧集优先：验证 `getEpisodeList()` 有结果

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 接口方法增多，实现复杂度上升 | 新源开发者需理解两种模式 | 提供清晰文档，默认实现返回 null |
| VM 状态字段增多，状态管理复杂 | 可能出现状态不一致 | 使用 sealed class 区分模式状态（后续优化） |
| 播放雷达需要两次判断（模式 + 内容） | 性能开销 | `isEpisodeFirstMode()` 无需重试，开销极小 |
| BottomSheet 在不同 Android 版本表现差异 | UI 一致性 | 使用 Material3 组件，统一表现 |

## Migration Plan

**部署步骤**：
1. 新增数据结构（EpisodeSimple, PlayLineSimple）
2. 扩展接口（PlayComponent）
3. 扩展包装器（CacheablePlayComponentWrapper）
4. 扩展 ViewModel（PlayLineIndexVM）
5. 新增/修改 UI 组件
6. 适配播放雷达

**回滚策略**：
- 所有新增方法均有默认实现（返回 null/false）
- 禁用剧集优先源即可回滚到原有行为
- 无需数据迁移

## Open Questions

（无）
