# KazumiRules batch migration

This document records the batch conversion from `Predidit/KazumiRules` to
EasyBangumi built-in Rhino JS sources.

## Source scope

- Upstream repository: `https://github.com/Predidit/KazumiRules`
- Verified upstream commit: `9663083`
- Target directory: `app/src/main/assets/inner_source/`
- Generated file prefix: `kazumi-`
- Source key prefix: `kazumi.`

The upstream rule repository contains 79 JSON files at the verified commit.
`index.json` is metadata and is not converted.

The user explicitly requested excluding `AGE.json` and `giriGiriLove.json`
because this project already has hand-maintained `age.js` and
`girigirilove.js`.

`XY.json` is also skipped because it is marked `deprecated: true` and its
required `searchResult` field is empty, so a reliable search-to-detail entry
cannot be generated.

Final result:

- 75 generated Kazumi JS sources
- no `kazumi-age.js`
- no `kazumi-girigirilove.js`
- existing `age.js`, `girigirilove.js`, and `xifandm.js` remain in
  `inner_source`

## Runtime support

Kazumi rules are XPath-based. Instead of mechanically rewriting XPath into
CSS selectors, the app now includes `JsoupXpath` and exposes a Java facade to
Rhino:

- dependency: `cn.wanghaomiao:JsoupXpath`
- facade: `XPathUtils`
- test: `XPathUtilsTest`

`XPathUtils` supports:

- selecting nodes from a Jsoup `Document`, Jsoup `Element`, or `JXNode`
- reading relative text and attributes
- reading an item node's own text or attribute
- extracting a first usable image URL from meta image tags or common lazy image
  attributes
- extracting a title fallback from meta title tags, `h1`, `h2`, or `title`

## Generated JS behavior

Each generated source contains:

- `@key kazumi.{safeName}`
- `@label` from the Kazumi rule name
- `@versionName` from the Kazumi rule version
- computed numeric `@versionCode`
- `@libVersion 15`
- `@cover {baseURL}/favicon.ico`

Search:

- requests `searchURL` with `@keyword` URL encoded
- supports normal GET and simple form POST rules
- selects result items with `searchList`
- extracts title with `searchName`
- extracts detail URL with `searchResult`
- stores the encoded detail URL in `CartoonCover.id`
- extracts cover from the search item with `XPathUtils.firstImage`

Detail:

- decodes `summary.id` back to the detail URL
- requests the detail page
- extracts cover with `XPathUtils.firstImage`
- extracts a title with `XPathUtils.title`
- selects play lines with `chapterRoads`
- selects episodes with `chapterResult`
- stores the encoded episode page URL in `Episode.id`
- skips empty play lines

Play:

- decodes `Episode.id` to the episode page URL
- calls `renderHelper.renderVideoFromJs(new JsVideoStrategy(...))`
- passes rule user-agent, referer headers, timeout, and `useLegacyParser`
- returns HLS when the rendered result reports `isM3u8`

The generated JS intentionally avoids Rhino-risky syntax:

- no ternary operator
- no `let` or `const`
- no arrow functions
- no template strings
- no optional chaining

## RenderHelper JS bridge

Rhino calls are routed through Java DTOs instead of Kotlin-only types:

- `JsRenderedStrategy`
- `JsRenderedResult`
- `JsVideoStrategy`
- `JsVideoResult`

`RenderHelperImpl` converts these Java DTOs to the internal Kotlin
`RenderedStrategy`, `RenderedResult`, `VideoStrategy`, and `VideoResult` types
before calling the existing implementation.

The generated Kazumi play functions use `renderVideoFromJs` so they do not
call Kotlin suspend/static wrappers directly from JS.

## Converter

The converter script is `tools/kazumi_json_to_js.py`.

It validates the required Kazumi fields:

- `name`
- `baseURL`
- `searchURL`
- `searchList`
- `searchName`
- `searchResult`
- `chapterRoads`
- `chapterResult`

It writes a complete EasyBangumi JS source using the shared template. Failed
validation means the rule should be skipped or handled manually.

## Verification

The final audit used these checks:

```text
kazumiFiles=75
hasKazumiAge=False
hasKazumiGiri=False
faviconCover=75
search=75
play=75
coverExtraction=75
```

The following Gradle tasks passed:

```text
./gradlew.bat :app:testDebugUnitTest --tests com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest
./gradlew.bat :app:testDebugUnitTest --tests com.heyanle.easybangumi4.plugin.source.js.utils.XPathUtilsTest
./gradlew.bat :app:compileDebugKotlin
```

`InnerJsSourceAssetTest` fixes the built-in source file list and evaluates all
inner JS files with Rhino. This covers metadata presence, supported
`libVersion`, JS syntax loading, and the requirement that JS uses injected Java
utility facades.

