## ADDED Requirements

### Requirement: Danmaku section communicates current state
The V2 playback-detail page SHALL show a peer-level `弹幕` section that communicates disabled, matching, matched, empty, unavailable, or failed state for the current playback episode and exposes an appropriate next action.

#### Scenario: No existing binding
- **WHEN** the current episode has no resolved danmaku binding
- **THEN** the danmaku section SHALL identify the source as DanDanPlay and expose automatic matching and manual search actions

#### Scenario: Matched episode
- **WHEN** danmaku comments have been resolved for the current playback episode
- **THEN** the section SHALL show the matched DanDanPlay bangumi and episode identity, comment availability, and a replacement action

### Requirement: 番剧匹配与选集匹配递进且边界明确
系统 SHALL 将弹幕匹配拆分为页面级番剧匹配和播放目标级选集匹配。详情与当前播放目标就绪后，系统 SHALL 至多自动匹配一次番剧；仅在番剧匹配成功后，系统 SHALL 自动匹配当前选集。番剧自动匹配只检查 DanDanPlay 返回的第一个番剧，且规范化标题的 Levenshtein 相似度必须严格大于 `0.80`。

#### Scenario: Reuse a prior binding
- **WHEN** the user returns to a playback episode with a valid saved DanDanPlay binding
- **THEN** the system SHALL restore its bangumi selection without repeating bangumi search, rematch the episode for the current sorted position, and load comments

#### Scenario: 番剧匹配成功后递进匹配选集
- **WHEN** DanDanPlay 首个搜索结果与本地番名的规范化相似度严格大于 `0.80`
- **THEN** 系统 SHALL 保留该番剧为当前页番剧选择，并 SHALL 按当前排序位置执行一次选集匹配

#### Scenario: 相似度处于边界或以下
- **WHEN** 首个搜索结果的标题相似度小于或等于 `0.80`
- **THEN** 系统 SHALL NOT 检查后续结果、加载远端选集或创建 binding，并 SHALL 提供手动匹配入口

#### Scenario: 当前排序位置越界
- **WHEN** 当前本地选集在当前排序投影中的一基位置不对应任何远端选集
- **THEN** 系统 SHALL NOT 创建 binding，并 SHALL 提供手动匹配入口

#### Scenario: 播放目标变化使用当下排序位置
- **WHEN** 用户切换播放线路、播放选集、手动进入下一集或自动播放下一集
- **THEN** 系统 SHALL 复用当前页已匹配番剧，只按该播放目标在当下排序投影中的位置匹配远端选集，不得读取本地 Episode 的 id、order 或 label 参与解析

### Requirement: 手动匹配复用已提交番剧并明确选择选集
系统 SHALL 提供可编辑番名查询、DanDanPlay 番剧候选和远端选集选择流程。当前页尚无已提交番剧时，流程 SHALL 从番剧步骤开始并要求用户依次选择番剧和选集；当前页已有自动、绑定恢复或手动提交的番剧时，流程 SHALL 同步该番剧的选中状态并直接进入选集步骤，且 SHALL 允许用户显式返回番剧步骤更换番剧。

#### Scenario: 自动匹配后打开手动面板
- **WHEN** 自动番剧匹配已经成功且用户打开弹幕匹配面板
- **THEN** 系统 SHALL 高亮选集步骤、展示已自动选择的番剧及其远端选集，并 SHALL NOT 重复搜索番剧或加载选集

#### Scenario: 番剧自动匹配失败后打开手动面板
- **WHEN** 当前页没有已提交番剧且用户打开弹幕匹配面板
- **THEN** 系统 SHALL 从番剧步骤开始，且 SHALL NOT 预选番剧或允许无番剧地提交选集

#### Scenario: User selects a remote episode
- **WHEN** the user selects a remote episode in the manual matching flow
- **THEN** the system SHALL persist the binding for the current playback identity, retain the manually selected bangumi for the current page, and load its comments

#### Scenario: 手动番剧在当前页内复用
- **WHEN** 用户完成手动番剧和选集选择后切换线路、选集或进入下一集
- **THEN** 系统 SHALL 保留该手动番剧并只执行自动选集匹配，不得重新执行自动番剧匹配

#### Scenario: Search returns no candidates
- **WHEN** a manual title search produces no bangumi candidates
- **THEN** the system SHALL retain the user's editable query and present an empty-result state without changing the current binding

### Requirement: Matching and comment retrieval are bounded and cache-aware
The system SHALL cache search, remote episode, and comment results with expiry and SHALL cancel or ignore stale matching/load work when the playback episode changes. Failures SHALL not disable video playback or replace an existing valid binding.

#### Scenario: Episode changes during loading
- **WHEN** the user changes playback episode before a matching or comment request finishes
- **THEN** the stale result SHALL NOT update the newly selected episode's danmaku state

#### Scenario: DanDanPlay is unavailable
- **WHEN** a DanDanPlay request fails because of network, authentication, quota, or service error
- **THEN** the system SHALL retain video playback, surface a retryable unavailable state, and preserve any existing valid binding
