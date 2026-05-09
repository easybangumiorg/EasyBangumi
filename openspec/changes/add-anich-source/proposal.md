## Why

AniCh 是一个提供动漫资源的网站，用户希望能够直接在 EasyBangumi 中观看 AniCh 的番剧。添加 AniCh 作为新的番剧源可以扩展应用的资源来源，为用户提供更多选择。

## What Changes

- 新增 AniCh 番剧源，使用剧集优先模式（Episode-First）
- 支持搜索功能，包括分页和 Cloudflare 验证码处理
- 支持获取剧集列表和播放源
- 支持通过 WebView 拦截视频资源获取播放地址
- 使用内存缓存提高性能（15分钟过期）

## Capabilities

### New Capabilities
- `anich-source`: AniCh 番剧源，包括搜索、剧集列表、播放源获取、播放地址获取等功能

### Modified Capabilities
（无）

## Impact

- 新增文件：
  - `shared/source/inner/.../anich/AniChInnerSource.kt`
  - `shared/source/inner/.../anich/AniChSearchComponent.kt`
  - `shared/source/inner/.../anich/AniChPlayComponent.kt`
  - `shared/source/inner/.../anich/AniChPrefComponent.kt`
- 修改文件：
  - `shared/source/.../core/inner/InnerSourceProvider.kt`（注册新源）
- 依赖：
  - WebViewHelper（用于页面加载和资源拦截）
  - NetworkHelper（用于 User-Agent）
  - PreferenceHelper（用于域名配置）
