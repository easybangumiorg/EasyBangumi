# Kazumi rules live test notes

This document records live verification for the generated Kazumi inner sources
under `app/src/main/assets/inner_source/`.

## Test scope

- Test date: 2026-06-24
- Device: MEIZU 21, Android 16
- Transport: `adb`
- Keyword: `孤独摇滚`
- Target sources: generated `kazumi-*.js`
- Interfaces checked:
  - home page, if the source provides a page component
  - search
  - detail, using the first search result
  - play, using the first episode from the first playable detail result
  - video URL validation, using URL shape and a bounded media probe

Early Android-side probes were temporary instrumentation tests and were removed
after execution. The current persisted Android black-box test is
`app/src/androidTest/java/com/heyanle/myapplication/easybangumi4/InnerJsSourceBlackBoxTest.kt`.

## Persisted black-box test plan

The persisted test follows the production loading path:

```text
SourceController.refresh()
  -> SourceBundle
  -> page/search/detailed/play component APIs
```

For each active generated Kazumi source it now checks:

- `SourceController` loads active `kazumi.*` sources and does not load
  `block-kazumi-*.js` assets.
- Home page, when `PageComponent` exists: load the first page and record count,
  first title, first cover, and whether title/cover are present. Kazumi sources
  currently have no page component, so home is recorded as `skip`.
- Search: query `孤独摇滚`, require non-empty results, and record the first
  result's title and cover.
- Detail: use the first search result, require at least one playable episode,
  and compare detail title/cover with the search result.
- Play: use the first episode from the first play line and require a non-empty
  URI.
- Video URL: reject script/html/player-wrapper URLs and probe direct
  `.m3u8`/`.mp4` URLs. A playlist beginning with `#EXTM3U` is accepted.
  Playlists whose first media entry looks like an image are marked
  `snapshot_playlist` for manual attention, because some video providers use
  image-like segment URLs as an anti-scraping scheme while the stream can still
  play.

The test emits one tab-separated `黑盒报告` line per source with Chinese fields:

```text
首页, 首页标签, 首页数量, 首页标题有效, 首页封面有效, 首页首个标题,
首页首个封面, 首页备注, 搜索, 详情, 播放, 标题匹配, 封面匹配,
搜索标题, 详情标题, 剧集, 播放地址, 视频, 视频说明, 错误
```

Each instrumentation test run also writes an independent `.tsv` report file on
the device. The file name includes the run date and time:

```text
/sdcard/Android/data/com.heyanle.easybangumi4.debug/files/inner_source_reports/kazumi_blackbox_yyyyMMdd_HHmmss_SSS.tsv
/sdcard/Android/data/com.heyanle.easybangumi4.debug/files/inner_source_reports/inner_home_search_yyyyMMdd_HHmmss_SSS.tsv
```

The test still prints the generated absolute path as a `报告文件` line. Reports
can be pulled from the device with:

```text
adb pull /sdcard/Android/data/com.heyanle.easybangumi4.debug/files/inner_source_reports/
```

`direct_m3u8_unverified` is accepted when the URL is a direct media entry but
the current network returns region-gated 403/404/504. Wrapper URLs such as
`https://player.example/?url=https://cdn.example/a.m3u8`, JS files such as
`static/player/*.js` are not accepted as real playable video.

The video validation found that `kazumi-omofun03.js` returns a legal m3u8 file
whose media entries are `.png` snapshot-looking resources. This is recorded as
`snapshot_playlist` but is not a hard failure, because some playable streams
use image-like segment URLs for anti-scraping.

The same instrumentation class also contains a broader
`activeInnerJsSourcesReportHomeAndSearchQuality` test for every active inner JS
source, including the hand-written sources `age.js`, `girigirilove.js`, and
`xifandm.js`. It uses the same `SourceController -> SourceBundle` path and
emits `首页搜索报告` lines with Chinese fields:

```text
首页, 首页标签, 首页数量, 首页标题有效, 首页封面有效, 首页首个标题,
首页首个封面, 首页备注, 搜索, 搜索数量, 搜索标题有效, 搜索封面有效,
搜索首个标题, 搜索首个封面, 搜索备注
```

This test is intentionally a quality report for home/search rather than a
strict gate. It records empty search results, missing covers, and network
failures for manual classification. The Kazumi-specific test remains the strict
gate for active Kazumi search/detail/play/video validity.

## Important probe correction

The first Android play probe used an Android mobile user-agent for rules whose
`USER_AGENT` field was empty. Generated Kazumi sources actually fall back to:

```text
Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.76
```

This caused at least `kazumi-7sefun.js` to be incorrectly reported as empty.
The later Android search retest used the same default Linux UA as the real
runtime and should be treated as the corrected result for search.

## Android search retest

Retested the sources that were previously reported as search failed or search
empty, plus Android candidates that failed in the first Android pass.

Summary:

```text
total=62
ok=4
empty=20
fail=38
timeout=0
```

Search success after retest:

| Source | Count | First title | First detail URL | First cover |
| --- | ---: | --- | --- | --- |
| `kazumi-7sefun.js` | 1 | `孤独摇滚！` | `https://www.7sefun.top/voddetail/29530.html` | `http://p.qpic.cn/music_cover/PiajxSqBRaEKia1eoHwIziaXrAdmbNMt62FtXrBAxHXs1XBCHv2Uzicib3g/600` |
| `kazumi-ant.js` | 3 | `孤独摇滚(上) 剧场总集篇` | `https://www.mayi520.org/voddetail/239583.html` | empty |
| `kazumi-dmghg.js` | 13 | `孤独摇滚！` | `https://www.dmghg1.com/index.php/vod/detail/id/66477.html` | `https://img.ffzy888.com/upload/vod/20221023-1/d3849d945d69cca4a62cc32b238c29a6.jpg` |
| `kazumi-mxdm.js` | 10 | empty | empty | empty |

Notes:

- `kazumi-7sefun.js` is confirmed searchable in the real Android environment.
- `kazumi-mxdm.js` selects 10 nodes, but first title and detail URL are empty.
  This likely means `searchList` matches a broad container while `searchName`
  or `searchResult` does not extract correctly from the first item.
- The remaining failed sources were mainly HTTP 403/404/444/500/522,
  SSL/certificate errors, DNS failures, connection failures, or socket
  timeouts. These should be treated as site/network/server issues until a
  browser/WebView-specific bypass is intentionally added.
- The remaining empty sources returned successful pages but the configured
  XPath did not produce result items for `孤独摇滚` in this run.

## Detail and play retest for search-ok sources

The 4 sources that became search-ok in the Android search retest were then
tested through detail and play on the same Android device with the corrected
default Linux UA.

Summary:

```text
total=4
searchOk=4
detailOk=3
playOk=2
```

| Source | Search | Detail | Play | Title match | Cover match | Episode | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `kazumi-7sefun.js` | ok, 1 result | ok | fail | true | false | `第01集` | Detail URL `https://www.7sefun.top/voddetail/29530.html`; episode URL `https://www.7sefun.top/vodplay/29530-1-1.html`; temporary WebView sniffer timed out. Detail cover resolved to site favicon instead of search cover. |
| `kazumi-ant.js` | ok, 3 results | ok | ok | true | false | `正片` | Detail URL `https://www.mayi520.org/voddetail/239583.html`; play sniffed `https://www.mayi520.org/static/player/hnm3u8.js?v=20230504`. Search and detail covers were empty. |
| `kazumi-dmghg.js` | ok, 13 results | ok | ok | true | false | `第01集` | Detail URL `https://www.dmghg1.com/index.php/vod/detail/id/66477.html`; play sniffed `https://vip.zykbf.com/?url=https://yzzy.play-cdn7.com/20221009/21929_100d2320/index.m3u8`. Detail cover did not match search cover. |
| `kazumi-mxdm.js` | ok, 10 nodes | skip | skip | false | false | empty | First matched search node had empty title and empty detail URL, so detail and play could not continue. This points to search XPath extraction problems, not a detail/play server failure. |

Interpretation:

- `kazumi-ant.js` and `kazumi-dmghg.js` pass search, detail, and WebView play
  sniffing in this probe.
- `kazumi-7sefun.js` passes search and detail, but play needs another pass with
  the app's real `RenderHelperImpl` before it is marked failed.
- `kazumi-mxdm.js` needs search XPath inspection first; the result node count is
  non-zero, but the first usable title/detail URL extraction is empty.
- The detail cover mismatch pattern remains present in these retested sources.

## Android play candidate probe

Before the corrected search retest, 20 sources that had passed JVM search,
detail, and episode-page checks were tested on Android with WebView sniffing.
Because that probe used the wrong fallback UA for empty `USER_AGENT` rules,
search failures in this section should not be used as final search results.
The play results for sources that reached detail are still useful as a rough
WebView sniffing signal.

Summary:

```text
total=20
searchOk=13
detailOk=13
playOk=11
titleAndCoverMatch=1
```

Search, detail, and play passed:

- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-baimao.js`
- `kazumi-enlie.js`
- `kazumi-fcdm.js`
- `kazumi-fqdm.js`
- `kazumi-gpjda.js`
- `kazumi-omofun03.js`
- `kazumi-skr.js`
- `kazumi-xfdmneo.js`
- `kazumi-ylsp.js`

Search and detail passed, play sniffing timed out:

- `kazumi-gugu3.js`
- `kazumi-yishijie.js`

These failures mean the episode page was reachable and detail parsing worked,
but the temporary WebView sniffer did not find a usable media URL within the
timeout. They need verification through the app's real `RenderHelperImpl`
before being marked as source failures.

## Detail title and cover accuracy

Title matching was generally acceptable for sources that reached detail.
Cover matching was poor:

- Only `kazumi-baimao.js` had search cover and detail cover matching in the
  Android play candidate probe.
- Many detail pages returned site logos, favicons, tracking images, or unrelated
  poster images through the generic `XPathUtils.firstImage(doc)` strategy.

This suggests detail cover extraction needs either source-specific detail cover
XPath support from the original Kazumi rule, or a stronger heuristic than
"first usable image in the document".

## Current takeaways

- The generated search/detail/play pipeline is viable for a subset of sources.
- Search testing must use the same fallback UA as production JS execution.
- `kazumi-7sefun.js` should be reclassified from search-empty to search-ok.
- Some "empty" results are likely XPath drift or selector mismatch, while many
  "fail" results are upstream/server/network failures.
- Detail cover extraction is the clearest common correctness issue found so far.

## Blocked source policy

After the Android retests, only sources that passed search, detail, and play
through the production loading path were kept active. Other generated Kazumi
files were renamed with the `block-` prefix under
`app/src/main/assets/inner_source/`.

`SourceController` skips any JS file whose filename starts with `block-`, so
blocked files remain in assets for future inspection or retest but are not
loaded into the runtime source bundle.

Active generated Kazumi sources after the `SourceController` black-box test:

- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-ant.js`
- `kazumi-baimao.js`
- `kazumi-enlie.js`
- `kazumi-gpjda.js`
- `kazumi-omofun03.js`
- `kazumi-ylsp.js`

Blocked generated Kazumi sources: 67.

The active set is also covered by an Android black-box test that loads sources
through `SourceController`, obtains components through `SourceBundle`, and calls
the public search/detail/play component APIs with `孤独摇滚`.

The black-box test is stricter than the temporary WebView sniffing probes. It
found that the following sources reached search and detail but returned an empty
play URI through the production `RenderHelperImpl` path, so they were moved to
`block-`:

- `kazumi-dmghg.js`
- `kazumi-fcdm.js`
- `kazumi-fqdm.js`
- `kazumi-skr.js`
- `kazumi-xfdmneo.js`

## Black-box play URL classification

The last successful black-box run before the stricter validator produced these
active-source play URLs:

| Source | URL | Classification |
| --- | --- | --- |
| `kazumi-9ciyuan.js` | `https://vip.dytt-kan.com/20250922/5851_2aec405d/index.m3u8` | Direct m3u8. Current desktop network returns region 403, but this is not a wrapper/script URL. |
| `kazumi-anime7.js` | `https://vip.dytt-kan.com/20250922/5851_2aec405d/index.m3u8` | Same as `kazumi-9ciyuan.js`. |
| `kazumi-baimao.js` | `https://vip.lz-cdn14.com/20221009/12985_d1e80e23/index.m3u8` | Direct m3u8. Current desktop network returns a domestic-network-only 404 page. |
| `kazumi-enlie.js` | `https://vip.lz-cdn14.com/20221009/12985_d1e80e23/index.m3u8` | Same as `kazumi-baimao.js`. |
| `kazumi-gpjda.js` | `https://yzzy.play-cdn7.com/20221009/21929_100d2320/index.m3u8` | Direct m3u8. Current desktop network returns region 403. |
| `kazumi-omofun03.js` | `https://s3plus.meituan.net/opapisdk/op_ticket_1_5677168484_1770979050190_qdqqd_idxkem.m3u8` | `snapshot_playlist`; curl returns `#EXTM3U`, but the first media entries are `.png` snapshot-looking URLs. Accepted with a warning because this can still be a playable anti-scraping stream. |
| `kazumi-ylsp.js` | `https://p.bvvvvvvvvv1f.com/video/guduyaogun/%E7%AC%AC01%E9%9B%86/index.m3u8` | Direct m3u8. Current desktop network returns an overseas-access 404 page. |
| `kazumi-ant.js` | `https://hn.bfvvs.com/play/kaz1XGZe/index.m3u8` | Confirmed hls; curl returns `#EXTM3U` with a nested m3u8 media entry. |

The validator keeps direct media URLs and snapshot-looking playlists, while
preventing player scripts and wrapper pages from being counted as playable
video.

## Latest verification

- `./gradlew :app:testDebugUnitTest --tests com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest`:
  passed with 8 active Kazumi sources and 67 blocked generated Kazumi sources.
- `./gradlew :app:compileDebugAndroidTestKotlin`: passed.
- `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.myapplication.easybangumi4.InnerJsSourceBlackBoxTest`:
  2026-06-24 14:42 在 MEIZU 21 真机通过，2 个测试、0 失败、0 错误，
  总耗时约 2 分钟。测试 XML 记录：
  `tests="2" failures="0" errors="0" skipped="0"`。

最新真机 connected run 使用生产加载链路
`SourceController.refresh() -> SourceBundle -> component API`，严格 Kazumi
黑盒门禁 8 个 active 源全部通过：

| 源 | 搜索 | 详情 | 播放 | 标题匹配 | 封面匹配 | 视频判断 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `kazumi-9ciyuan.js` | 通过 | 通过 | 通过 | 是 | 否 | 确认HLS | 媒体分片 `3000k/hls/mixed.m3u8` |
| `kazumi-anime7.js` | 通过 | 通过 | 通过 | 是 | 否 | 确认HLS | 媒体分片 `3000k/hls/mixed.m3u8` |
| `kazumi-baimao.js` | 通过 | 通过 | 通过 | 是 | 是 | 确认HLS | 媒体分片 `1200k/hls/mixed.m3u8` |
| `kazumi-enlie.js` | 通过 | 通过 | 通过 | 是 | 否 | 确认HLS | 媒体分片 `1200k/hls/mixed.m3u8` |
| `kazumi-gpjda.js` | 通过 | 通过 | 通过 | 是 | 否 | 确认HLS | 媒体分片 `1000k/hls/mixed.m3u8` |
| `kazumi-omofun03.js` | 通过 | 通过 | 通过 | 是 | 否 | 图片分片播放列表 | 首个媒体分片疑似 `.png`，按反爬场景保留为成功并提示 |
| `kazumi-ylsp.js` | 通过 | 通过 | 通过 | 是 | 否 | 直接M3U8待验证 | 有直接 `.m3u8`，本次有界探测返回 HTTP 504 |
| `kazumi-ant.js` | 通过 | 通过 | 通过 | 是 | 是 | 直接M3U8待验证 | 有直接 `.m3u8`，本次有界探测返回 HTTP 504 |

本次播放地址判断没有把脚本、HTML 或播放器包装页计为有效视频地址。
`kazumi-omofun03.js` 的首个媒体是图片分片，已按用户说明标为可能成功的
反爬播放列表；`kazumi-ylsp.js` 和 `kazumi-ant.js` 是直接 m3u8，但本轮
网络探测为 504，所以标记为待验证而非硬失败。

同一轮 connected run 中，`activeInnerJsSourcesReportHomeAndSearchQuality`
首页/搜索质量测试也执行通过，用例耗时 133 秒。后续尝试单独重跑该用例以
重新抓取 `首页搜索报告` 宽表时，设备安装策略再次返回
`INSTALL_FAILED_USER_RESTRICTED`，因此没有覆盖本轮通过结论。

## Full Kazumi black-box run

2026-06-24 15:11 在 MEIZU 21 真机运行
`allKazumiSourcesReportSearchDetailPlayQuality`。该用例临时加载 assets
中的所有 `kazumi-*.js` 与 `block-kazumi-*.js`，不修改正式运行时的
`block-` 策略；调用链仍为 `SourceController -> SourceBundle -> component
API`。

原始报告与中文汇总：

- `docs/test_reports/kazumi_all_20260624_151100_651.tsv`
- `docs/test_reports/kazumi_all_20260624_151100_651_summary.md`

本轮总计 75 个 Kazumi JS：

- 搜索通过：17；搜索为空：27；搜索失败：31。
- 详情通过：16。
- 播放通过：9。

完整通过搜索、详情、播放的源：

- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-ant.js`
- `kazumi-baimao.js`
- `kazumi-enlie.js`
- `kazumi-gpjda.js`
- `kazumi-omofun03.js`
- `kazumi-yishijie.js`
- `kazumi-ylsp.js`

部分通过但未达到播放通过：

- `kazumi-7sefun.js`: 搜索和详情通过，播放地址为非直接 m3u8/mp4，标为
  `未知地址`。
- `kazumi-dmghg.js`, `kazumi-fcdm.js`, `kazumi-fqdm.js`, `kazumi-gugu3.js`,
  `kazumi-skr.js`, `kazumi-xfdmneo.js`: 搜索和详情通过，但播放 URI 为空。
- `kazumi-mxdm.js`: 搜索通过，但详情无播放线路或剧集。

当前 connected 测试命令：

```text
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.heyanle.myapplication.easybangumi4.InnerJsSourceBlackBoxTest
```

## Latest full Kazumi black-box run

2026-06-24 16:44 在 MEIZU 21 真机再次运行
`allKazumiSourcesReportSearchDetailPlayQuality`。本轮将 assets
`inner_source` 中全部 Kazumi JS 纳入测试，包括 44 个启用源和 31 个
`block-` 源。测试对 `block-` 文件创建去前缀副本后通过测试专用
`SourceController -> SourceBundle -> component API` 加载，不改变正式运行时
`SourceController` 跳过 `block-` 前缀的策略。

原始报告与中文汇总：

- `docs/test_reports/kazumi_all_20260624_164413_296.tsv`
- `docs/test_reports/kazumi_all_20260624_164413_296_summary.md`

Runner 结果：1 个 instrumentation 用例通过，总耗时 480.167 秒。

本轮总计 75 个 Kazumi JS：

- 搜索通过：16；搜索为空：27；搜索失败：32。
- 详情通过：16。
- 播放通过：10；播放空：6。
- 标题匹配：16；封面匹配：16。
- 视频判断：确认 HLS 7 个，确认视频 2 个，图片分片播放列表 1 个。

完整通过搜索、详情、播放的源：

- `kazumi-7sefun.js`
- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-ant.js`
- `kazumi-baimao.js`
- `kazumi-gpjda.js`
- `kazumi-mxdm.js`
- `kazumi-omofun03.js`
- `kazumi-yishijie.js`
- `kazumi-ylsp.js`

搜索和详情通过但播放 URI 为空的源：

- `kazumi-dmghg.js`
- `kazumi-fcdm.js`
- `kazumi-fqdm.js`
- `kazumi-gugu3.js`
- `kazumi-skr.js`
- `kazumi-xfdmneo.js`

这些播放空结果暂时按 `RenderHelperImpl` 嗅探时序、播放器包装页、iframe
跳转或站点反爬继续排查，不在本轮直接修改 JS 源。

其他结论：

- 31 个 `block-` 源本轮全部搜索失败。其中 21 个在请求失败分支触发
  `ParserException` 构造器签名问题，另外 10 个为连接失败、超时、DNS
  或 SSL 问题。
- 启用源 `kazumi-anfuns.js` 本轮搜索失败，错误为 HTTP 流异常中断。
- `kazumi-omofun03.js` 的首个媒体分片是图片，已按反爬播放列表标注为
  可能可播。
- 本轮所有 16 个详情通过源的详情标题和封面均与搜索结果匹配。

## Latest full Kazumi black-box run after repackaging

2026-06-24 17:45 在 MEIZU 21 真机运行最终轮次
`allKazumiSourcesReportSearchDetailPlayQuality`。本轮先重新打包并安装包含
最新 assets JS 的 debug APK，再安装修正报告逻辑后的 androidTest APK，
然后通过 `adb shell am instrument` 触发。测试范围仍为 assets
`inner_source` 下全部 Kazumi JS，包括 44 个启用源和 31 个 `block-` 源；
测试时对 `block-` 文件创建去前缀副本后通过测试专用
`SourceController -> SourceBundle -> component API` 加载，不改变正式运行时
`SourceController` 跳过 `block-` 前缀的策略。

原始报告与中文汇总：

- `docs/test_reports/kazumi_all_20260624_174511_631.tsv`
- `docs/test_reports/kazumi_all_20260624_174511_631_summary.md`

Runner 结果：1 个 instrumentation 用例通过，总耗时 507.369 秒。

本轮总计 75 个 Kazumi JS：

- 资产状态：启用 44，block 31。
- 搜索通过：16；搜索为空：28；搜索失败：31。
- 详情通过：16。
- 播放通过：10；播放失败：6。
- 标题匹配：16；封面匹配：16。
- 视频判断：确认 HLS 7 个，确认视频 2 个，图片分片播放列表 1 个。
- 旧的 `ParserException` 单参数构造器错误计数为 0，说明重新打包后的 JS
  已生效。

完整通过搜索、详情、播放，并且播放地址通过真实性判断的源：

- `kazumi-7sefun.js`: 确认视频，`video/mp4`。
- `kazumi-9ciyuan.js`: 确认 HLS，媒体分片 `3000k/hls/mixed.m3u8`。
- `kazumi-anime7.js`: 确认 HLS，媒体分片 `3000k/hls/mixed.m3u8`。
- `kazumi-ant.js`: 确认 HLS，媒体分片 `/play/hls/kaz1XGZe/index.m3u8`。
- `kazumi-baimao.js`: 确认 HLS，媒体分片 `1200k/hls/mixed.m3u8`。
- `kazumi-gpjda.js`: 确认 HLS，媒体分片 `1000k/hls/mixed.m3u8`。
- `kazumi-mxdm.js`: 确认 HLS，媒体分片 `2000k/hls/mixed.m3u8`。
- `kazumi-omofun03.js`: 图片分片播放列表；首个媒体分片疑似图片，按反爬
  场景标注为可能可播。
- `kazumi-yishijie.js`: 确认视频，`video/mp4`。
- `kazumi-ylsp.js`: 确认 HLS，媒体分片 `0000000.ts`。

搜索和详情通过但播放失败的源：

- `kazumi-dmghg.js`: legacy=true，播放页
  `https://www.dmghg1.com//index.php/vod/play/id/66477/sid/2/nid/1.html`。
- `kazumi-fcdm.js`: legacy=true，播放页
  `https://fcdm.org.cn//v/36605-1-1/`。
- `kazumi-fqdm.js`: legacy=true，播放页
  `https://www.fqdm.cc//index.php/vod/play/id/11372/sid/1/nid/1.html`。
- `kazumi-gugu3.js`: legacy=false，播放页
  `https://www.gugu3.com//index.php/vod/play/id/94/sid/2/nid/1.html`。
- `kazumi-skr.js`: legacy=false，播放页
  `https://skr.skr1.cc:666//vodplay/187445-2-1/`。
- `kazumi-xfdmneo.js`: legacy=true，播放页
  `https://dm1.xfdm.pro//watch/3202/1/1.html`。

这 6 个源的详情标题和封面均与搜索结果匹配，但 `renderVideoFromJs` 和
`player_aaaa.url` 直链兜底都没有产出可播放 URI。legacy=true 的源倾向
`RenderHelperImpl` legacy 路径、播放器脚本、iframe 或站点反爬边界；legacy=false
的 `gugu3/skr` 可能需要播放器脚本、token 或点击行为解析。本轮按用户要求先
沉淀原因，不直接修改源逻辑。

其他结论：

- 28 个启用源搜索为空，详情和播放按流程跳过。
- 31 个 `block-` 源全部搜索失败，主要是 HTTP 4xx/5xx、连接超时、连接
  失败、DNS 或 SSL 问题。
- 桌面端辅助 `curl` 抽查播放失败页时遇到 DNS 解析失败，因此播放失败的
  最终判断以本轮安卓真机 TSV 为准。

## Collaboration split

本轮按多角色分工推进：

- 主线实现/集成：负责 Kazumi JS 模板、资产源、`SourceController` block
  加载策略、Android 黑盒测试和最终文档沉淀。
- RenderHelper 分析角色：聚焦 `RenderHelperImpl` 与 legacy/non-legacy 播放
  嗅探边界，结论是 `renderVideoFromJs` 在 legacy iframe、播放器脚本、token
  或点击行为场景中可能返回空；这类问题先沉淀为工具/站点边界，不直接改 JS。
- 验证/审计角色：复核当前工作树是否满足 9 active / 66 block、正式加载跳过
  `block-`、测试与报告证据一致。

## Final active/block policy

根据 2026-06-24 18:29 的最新真机全量黑盒报告、2026-06-24 18:10 的正式
active 门禁复测，以及失败源直接请求复核，正式运行时只保留搜索、详情、
播放、标题/封面匹配、视频地址真实性在当前门禁中稳定通过的 9 个 Kazumi 源
为 active。其余 66 个 Kazumi JS 已统一改为 `block-kazumi-*.js`，继续保留在
assets 中供后续全量黑盒复测，但不会被正式 `SourceController` 加载。

最终 active Kazumi 源：

- `kazumi-7sefun.js`
- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-ant.js`
- `kazumi-baimao.js`
- `kazumi-gpjda.js`
- `kazumi-mxdm.js`
- `kazumi-omofun03.js`
- `kazumi-ylsp.js`

最终 block Kazumi 源：66 个，包括所有搜索为空、搜索失败、搜索/详情通过但
播放失败的源，以及在 active 门禁复测中播放不稳定的 `kazumi-yishijie.js`。
`dmghg/fcdm/fqdm/gugu3/skr/xfdmneo/yishijie` 已按 RenderHelperImpl 或播放器
脚本/站点反爬边界沉淀，未继续修改 JS 逻辑。

对应测试门禁已同步：

- JVM 资产测试只允许 9 个 active Kazumi，并断言 66 个 blocked Kazumi。
- Android stable 黑盒测试只验证这 9 个 active Kazumi。
- Android all-Kazumi 黑盒测试仍会通过测试专用目录把 `block-` 前缀去掉，
  覆盖全部 75 个 Kazumi JS，便于后续重新评估被 block 的源。

最终 stable 门禁验证：

- `./gradlew :app:testDebugUnitTest --tests com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest`：通过。
- `./gradlew :app:compileDebugAndroidTestKotlin`：通过。
- `adb shell am instrument -w -r -e class com.heyanle.myapplication.easybangumi4.InnerJsSourceBlackBoxTest#stableKazumiSourcesPassSearchDetailPlay com.heyanle.easybangumi4.debug.test/androidx.test.runner.AndroidJUnitRunner`：
  2026-06-24 18:10 在 MEIZU 21 真机通过，1 个测试、0 失败，总耗时 45.252 秒。
- 原始报告：`docs/test_reports/kazumi_blackbox_20260624_181032_205.tsv`。

本轮 9 个 active 源全部通过搜索、详情、播放、标题/封面匹配和视频地址判断：

- `kazumi-7sefun.js`: 确认视频，`video/mp4`。
- `kazumi-9ciyuan.js`: 确认 HLS，媒体分片 `3000k/hls/mixed.m3u8`。
- `kazumi-anime7.js`: 确认 HLS，媒体分片 `3000k/hls/mixed.m3u8`。
- `kazumi-ant.js`: 确认 HLS，媒体分片 `/play/hls/kaz1XGZe/index.m3u8`。
- `kazumi-baimao.js`: 确认 HLS，媒体分片 `1200k/hls/mixed.m3u8`。
- `kazumi-gpjda.js`: 确认 HLS，媒体分片 `1200k/hls/mixed.m3u8`。
- `kazumi-mxdm.js`: 确认 HLS，媒体分片 `2000k/hls/mixed.m3u8`。
- `kazumi-omofun03.js`: 图片分片播放列表；首个媒体分片疑似图片，按反爬
  场景标注为可能可播。
- `kazumi-ylsp.js`: 确认 HLS，媒体分片 `0000000.ts`。

## Full Kazumi black-box run 20260624_182914_192

2026-06-24 18:29 在 MEIZU 21 真机运行
`allKazumiSourcesReportSearchDetailPlayQuality`。本轮先重新打包并安装 debug
APK 和 androidTest APK，然后通过 `adb shell am instrument` 触发。测试范围为
assets `inner_source` 下全部 Kazumi JS，共 75 个；其中正式启用 9 个，
`block-` 66 个。测试时对 `block-` 文件创建去前缀副本，通过测试专用
`SourceController -> SourceBundle -> component API` 加载，不改变正式运行时
`SourceController` 跳过 `block-` 前缀的策略。

原始报告与中文汇总：

- `docs/test_reports/kazumi_all_20260624_182914_192.tsv`
- `docs/test_reports/kazumi_all_20260624_182914_192_summary.md`
- `docs/test_reports/kazumi_http_probe_20260624_182914_192.tsv`

Runner 结果：1 个 instrumentation 用例通过，总耗时 437.465 秒。

本轮总计 75 个 Kazumi JS：

- 资产状态：启用 9，block 66。
- 搜索通过：17；搜索为空：27；搜索失败：31。
- 详情通过：17。
- 播放通过：11；播放失败：6；跳过：58。
- 视频判断：确认 HLS 7 个，确认视频 2 个，图片分片播放列表 2 个。
- 9 个正式启用源全部通过搜索、详情、播放、标题/封面匹配和视频地址判断。

正式启用且完整通过的源：

- `kazumi-7sefun.js`
- `kazumi-9ciyuan.js`
- `kazumi-anime7.js`
- `kazumi-ant.js`
- `kazumi-baimao.js`
- `kazumi-gpjda.js`
- `kazumi-mxdm.js`
- `kazumi-omofun03.js`
- `kazumi-ylsp.js`

`block-` 源中本轮也完整通过的源：

- `block-kazumi-enlie.js`: 图片分片播放列表；首个媒体分片疑似图片，按反爬
  场景标注为可能可播，仍保持 block。
- `block-kazumi-yishijie.js`: 确认视频，`video/mp4`；由于此前 active 门禁中
  发生过播放不稳定，仍保持 block，建议多轮稳定后再考虑恢复。

搜索和详情通过但播放失败的源：

- `block-kazumi-dmghg.js`: legacy=true，播放页
  `https://www.dmghg1.com//index.php/vod/play/id/66477/sid/2/nid/1.html`。
- `block-kazumi-fcdm.js`: legacy=true，播放页
  `https://fcdm.org.cn//v/36605-1-1/`。
- `block-kazumi-fqdm.js`: legacy=true，播放页
  `https://www.fqdm.cc//index.php/vod/play/id/11372/sid/1/nid/1.html`。
- `block-kazumi-gugu3.js`: legacy=false，播放页
  `https://www.gugu3.com//index.php/vod/play/id/94/sid/2/nid/1.html`。
- `block-kazumi-skr.js`: legacy=false，播放页
  `https://skr.skr1.cc:666//vodplay/187445-2-1/`。
- `block-kazumi-xfdmneo.js`: legacy=true，播放页
  `https://dm1.xfdm.pro//watch/3202/1/1.html`。

这 6 个源的详情标题和封面均与搜索结果匹配，但 `renderVideoFromJs` 和
`player_aaaa.url` 直链兜底都没有产出可播放 URI。legacy=true 的源倾向
`RenderHelperImpl` legacy 路径、播放器脚本、iframe 或站点反爬边界；legacy=false
的 `gugu3/skr` 可能需要播放器脚本、token 或点击行为解析。本轮按用户要求先
沉淀原因，不直接修改源逻辑。

对搜索未通过的 58 个源，本轮额外使用规则中的 `SEARCH_URL` 拼接
`孤独摇滚` 做直接请求复核。可确认 HTTP 4xx/5xx、SSL、超时、域名停放、JS
防护页或 Cloudflare 的源按站点/服务器问题继续放弃；直接搜索页含关键词但
规则未提取结果的 8 个源单列为规则/选择器待复查：

- `block-kazumi-eacg.js`
- `block-kazumi-fantuan.js`
- `block-kazumi-hfkzm.js`
- `block-kazumi-k8dm.js`
- `block-kazumi-pekolove.js`
- `block-kazumi-qifun.js`
- `block-kazumi-qkan9.js`
- `block-kazumi-xigua.js`
