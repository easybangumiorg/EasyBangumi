# KazumiRules 迁移准备

目标：按单个源逐个把 `Predidit/KazumiRules` 中的 Kazumi JSON 规则迁移为纯纯看番 JS 内置源，并放入 `app/src/main/assets/inner_source/`。

不要批量迁移。每次只迁移用户明确指定的一个规则文件。

## 文件位置

- Kazumi 原规则来源：`https://raw.githubusercontent.com/Predidit/KazumiRules/main/{name}.json`
- 纯纯看番内置源目录：`app/src/main/assets/inner_source/`
- 纯纯看番 JS 源 loader：`JsSourceFileLoader`
- 纯纯看番 JS 源运行时：Rhino + `JSComponentBundle`

内置源加载流程：

1. `InnerSourceFileProvider` 扫描 `assets/inner_source/`。
2. 仅复制 `.js` 到 app 私有缓存目录。
3. `SourceController` 将内置源文件和用户源文件合并。
4. 使用 `JsSourceFileLoader` 读取元数据并创建 `JsSource`。
5. 进入现有 `ConfigSource -> SourceBundle` 数据流。

## Kazumi 字段映射

| Kazumi 字段 | 纯纯看番 JS 实现 | 备注 |
| --- | --- | --- |
| `name` | 顶部 `// @key`, `// @label` | `key` 建议使用 `kazumi.{name}`，`label` 使用原名 |
| `version` | 顶部 `// @versionName`, `// @versionCode` | 没有数字版本时人工递增 `versionCode` |
| `baseURL` | JS 常量 `BASE_URL` | 必填 |
| `userAgent` | 请求或渲染时传入 `NetworkHelper.defaultLinuxUA` / 自定义 UA | 空字符串转为默认 UA |
| `searchURL` | `SearchComponent_search(pageKey, keyword)` | 使用 `encodeURIComponent` 或 `URLEncoder.encode` 处理关键词 |
| `searchList` | `doc.select(...)` 或 XPath 工具 | 按站点实际 HTML 选择合适解析方式 |
| `searchName` | `makeCartoonCover({ title: ... })` | 需要返回 `ArrayList<CartoonCover>` |
| `searchResult` | `makeCartoonCover({ id, url })` | `id` 应是稳定业务 id，不要为了播放偷塞 URL |
| `chapterRoads` | `DetailedComponent_getDetailed` 中构造 `ArrayList<PlayLine>` | `PlayLine.id` 和 `label` 都应显式来自页面或规则 |
| `chapterResult` | `new Episode(id, label, order)` | `Episode.id` 应是分集业务 id，播放 URL 在 play 阶段拼接 |
| `useLegacyParser` | `RenderHelper.VideoStrategy(..., useLegacyParser = true)` | 仅当站点需要 legacy iframe 解析时使用 |
| `usePost` | JS 中手写 OkHttp POST | 按具体规则处理 |
| `referer` | `HashMap` headers 传入请求或渲染 | 如规则有 referer，加入 headers |
| `antiCrawlerConfig` | JS 中按需使用 `CaptchaHelper` / `WebProxyProvider` | 需要按具体站点转验证码/WebView/headers 策略 |

## 单个源迁移步骤

1. 拉取目标规则：
   - `index.json` 用来确认规则名、版本、是否启用 native player/anti crawler。
   - `{name}.json` 用来转换字段。
2. 创建文件：
   - 路径：`app/src/main/assets/inner_source/kazumi-{safe-name}.js`
   - 顶部必须包含 `@key`, `@label`, `@versionName`, `@versionCode`, `@libVersion`
   - `@libVersion`: 当前 `PluginV3.MAX_SUPPORTED_LIB_VERSION`
3. 转换搜索：
   - 实现 `SearchComponent_search(pageKey, keyword)`。
   - 返回 `new Pair(nextKey, ArrayList<CartoonCover>)`。
   - `nextKey` 仅在源支持翻页且 URL 可表达时返回。
4. 转换详情：
   - 实现 `DetailedComponent_getDetailed(summary)`。
   - 返回 `new Pair(cartoon, ArrayList<PlayLine>)`。
   - `PlayLine.id`, `PlayLine.label`, `Episode.id`, `Episode.label`, `Episode.order` 都要显式维护。
5. 转换播放：
   - 实现 `PlayComponent_getPlayInfo(summary, playLine, episode)`。
   - 使用 `summary.id`, `playLine.id`, `episode.id` 拼播放页或直链。
   - 如果需要网页嗅探，调用 `RenderHelper.renderVideo(new RenderHelper.VideoStrategy(...))`。
   - 有 legacy parser 需求时将 `useLegacyParser` 设为 `true`。
6. 验证：
   - `./gradlew :app:compileDebugKotlin -Pksp.incremental=false`
   - `./gradlew :app:testDebugUnitTest --tests "com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest" -Pksp.incremental=false`
   - 如新增了专门测试，也运行对应测试。
7. 审计：
   - 确认只新增/修改一个内置源规则。
   - 确认没有把 Kazumi 原始 JSON 直接塞进 `inner_source`。
   - 确认没有批量迁移其他规则。

## 注意事项

- Kazumi XPath 规则需要结合真实页面重新验证，不能机械替换后就认为完成。
- POST 搜索规则在 JS 中手写 OkHttp 请求。
- HLS 广告过滤、复杂 anti-crawler、WebView 手动验证等能力需要按单个源实际行为评估。
- `assets/inner_source/` 只放 `.js`，加载链路只支持 JS 源。
