## Context

AniCh 是一个提供动漫资源的网站，使用 Vue.js SPA 构建，后端 API 使用 Protocol Buffers 通信。网站有 Cloudflare 保护，需要处理验证码。用户希望在 EasyBangumi 中添加 AniCh 作为新的番剧源。

当前 EasyBangumi 已支持剧集优先模式（Episode-First），AniCh 的数据结构天然适合这种模式：先获取剧集列表，再获取每个剧集的播放源。

## Goals / Non-Goals

**Goals:**
- 实现 AniCh 番剧源，支持搜索、剧集列表、播放源获取、播放地址获取
- 使用剧集优先模式（Episode-First）
- 支持分页搜索
- 支持 Cloudflare 验证码处理
- 使用内存缓存提高性能（15分钟过期）

**Non-Goals:**
- 不支持无限滚动分页
- 不支持播放失败自动重试
- 不实现 protobuf 编解码（使用 WebView 解析 window.$data）

## Decisions

### 决策 1：数据获取方式

**选择**：使用 WebView 解析 window.$data，而非直接调用 protobuf API

**理由**：
- AniCh 使用 protobuf 通信，实现编解码需要额外依赖
- window.$data 已包含所有需要的数据（搜索结果、剧集列表、播放源）
- WebView 方式更简单，易于维护

**替代方案**：
- 直接调用 protobuf API：需要引入 protobuf 依赖，实现复杂

### 决策 2：播放地址获取

**选择**：使用 WebView 拦截 m3u8/mp4 请求

**理由**：
- AniCh 使用 hls.js 播放视频，视频 URL 通过网络请求加载
- 拦截请求是最可靠的方式
- 参考现有源（如 GGL）的实现

**替代方案**：
- 解析页面中的视频元素：可能无法获取到完整的 URL

### 决策 3：缓存策略

**选择**：内存缓存，15分钟过期

**理由**：
- 减少重复的 WebView 加载
- 15分钟平衡了性能和数据新鲜度
- 实现简单，无需持久化

**替代方案**：
- 无缓存：每次都需要加载页面，性能差
- 持久化缓存：增加复杂度，数据可能过期

### 决策 4：验证码处理

**选择**：抛出 NeedWebViewCheckException，支持用户手动验证

**理由**：
- 参考 GGL 源的实现
- 用户手动验证是最可靠的方式
- 验证完成后通过 searchWithCheck 继续

### 决策 5：播放源映射

**选择**：sites 数组中的每个元素作为一个播放线路

**理由**：
- AniCh 的 sites 包含 tmdb、bangumi 等多个源
- 每个源可以作为独立的播放线路
- 按顺序返回，无需优先级

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| window.$data 结构变化 | 数据解析失败 | 添加异常处理，返回空列表 |
| Cloudflare 验证频繁 | 用户体验差 | 实现验证码处理，支持手动验证 |
| 播放地址拦截失败 | 无法播放 | 增加超时时间（10000ms） |
| 内存缓存溢出 | 性能下降 | 限制缓存大小，实现 LRU 策略 |
| WebView 加载慢 | 响应延迟 | 使用缓存减少加载次数 |

## Migration Plan

**部署步骤**：
1. 创建 AniCh 源目录和文件
2. 实现 AniChInnerSource、AniChPrefComponent
3. 实现 AniChSearchComponent（搜索、分页、验证码）
4. 实现 AniChPlayComponent（剧集优先模式、缓存）
5. 注册源到 InnerSourceProvider
6. 测试验证

**回滚策略**：
- 移除 InnerSourceProvider 中的注册
- 删除 anich 目录

## Open Questions

（无）
