## ADDED Requirements

### Requirement: 搜索功能

系统 SHALL 支持 AniCh 番剧搜索，包括分页和 Cloudflare 验证码处理。

#### Scenario: 搜索成功
- **WHEN** 用户输入关键词进行搜索
- **THEN** 系统返回匹配的番剧列表，包含 id、名称、封面、简介

#### Scenario: 搜索分页
- **WHEN** 搜索结果非空
- **THEN** 系统返回下一页的 key，支持翻页

#### Scenario: 搜索结果为空
- **WHEN** 搜索没有匹配结果
- **THEN** 系统返回空列表，nextKey 为 null

#### Scenario: Cloudflare 验证
- **WHEN** 搜索时遇到 Cloudflare 验证
- **THEN** 系统抛出 NeedWebViewCheckException，支持用户手动验证

### Requirement: 剧集列表获取

系统 SHALL 支持获取 AniCh 番剧的剧集列表，使用剧集优先模式。

#### Scenario: 获取剧集列表
- **WHEN** 用户打开番剧详情
- **THEN** 系统返回该剧集的列表，包含序号、标题、状态

#### Scenario: 剧集状态筛选
- **WHEN** 剧集 status 为 true
- **THEN** 该剧集被包含在结果中

#### Scenario: 剧集状态过滤
- **WHEN** 剧集 status 为 false
- **THEN** 该剧集被过滤掉，不包含在结果中

#### Scenario: 缓存命中
- **WHEN** 15分钟内重复请求同一番剧的剧集列表
- **THEN** 系统直接返回缓存数据，不重新加载页面

### Requirement: 播放源获取

系统 SHALL 支持获取指定剧集的播放源列表。

#### Scenario: 获取播放源
- **WHEN** 用户选择一个剧集
- **THEN** 系统返回该剧集的播放源列表，包含 tmdb、bangumi 等

#### Scenario: 播放源为空
- **WHEN** 剧集没有播放源
- **THEN** 系统返回空列表

#### Scenario: 缓存命中
- **WHEN** 15分钟内重复请求同一剧集的播放源
- **THEN** 系统直接返回缓存数据

### Requirement: 播放地址获取

系统 SHALL 支持获取指定播放源的播放地址。

#### Scenario: 获取播放地址
- **WHEN** 用户选择一个播放源
- **THEN** 系统返回可播放的视频 URL 和类型（HLS 或 NORMAL）

#### Scenario: 播放地址拦截
- **WHEN** 播放页面加载完成
- **THEN** 系统拦截 m3u8/mp4 请求，获取视频 URL

#### Scenario: 播放地址获取失败
- **WHEN** 无法拦截到视频资源
- **THEN** 系统抛出 DataStateException

### Requirement: 偏好配置

系统 SHALL 支持 AniCh 域名配置。

#### Scenario: 默认域名
- **WHEN** 用户未配置域名
- **THEN** 系统使用默认域名 anich.emmmm.eu.org

#### Scenario: 自定义域名
- **WHEN** 用户配置自定义域名
- **THEN** 系统使用用户配置的域名

### Requirement: 剧集优先模式

AniCh 源 SHALL 使用剧集优先模式（Episode-First）。

#### Scenario: 模式判断
- **WHEN** 系统调用 isEpisodeFirstMode
- **THEN** 返回 true

#### Scenario: 数据流
- **WHEN** 用户浏览番剧
- **THEN** 先显示剧集列表，选择剧集后再显示播放源
