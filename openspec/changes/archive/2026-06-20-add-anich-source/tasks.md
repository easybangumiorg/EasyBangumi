## 1. 基础结构

- [x] 1.1 创建 anich 目录结构
- [x] 1.2 实现 AniChInnerSource.kt（源定义、组件注册、Koin 模块）
- [x] 1.3 实现 AniChPrefComponent.kt（域名配置偏好）

## 2. 搜索功能

- [x] 2.1 实现 AniChSearchComponent.kt 基础结构
- [x] 2.2 实现 firstKey() 和分页逻辑
- [x] 2.3 实现 WebView 搜索和 window.$data 解析
- [x] 2.4 实现 Cloudflare 验证码检测和处理
- [x] 2.5 实现 searchWithCheck 方法

## 3. 播放组件

- [x] 3.1 实现 AniChPlayComponent.kt 基础结构
- [x] 3.2 实现 isEpisodeFirstMode() 返回 true
- [x] 3.3 实现 getEpisodeList()（剧集列表获取）
- [x] 3.4 实现 getPlayLineSimpleForEpisode()（播放源获取）
- [x] 3.5 实现 getPlayInfoSimple()（播放地址获取）
- [x] 3.6 实现缓存机制（15分钟过期）

## 4. 注册和测试

- [x] 4.1 在 InnerSourceProvider 中注册 AniCh 源
- [x] 4.2 测试搜索功能
- [x] 4.3 测试剧集列表获取
- [x] 4.4 测试播放源获取
- [x] 4.5 测试播放地址获取
