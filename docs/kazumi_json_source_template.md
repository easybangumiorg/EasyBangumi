# Kazumi JSON source template

This note describes the reusable template used by
`tools/kazumi_json_to_js.py`.

## Purpose

Kazumi rules describe source behavior with JSON fields and XPath expressions.
EasyBangumi built-in sources are Rhino JS files. The converter maps each valid
Kazumi rule to one Rhino-compatible JS file that can be placed in
`app/src/main/assets/inner_source/`.

The generated source focuses on the shared Kazumi flow:

- search
- detail and play-line extraction
- episode page playback sniffing
- cover extraction from search/detail HTML

Homepage support is intentionally not generated.

## Required Kazumi fields

The converter requires these fields to be non-empty:

- `name`
- `baseURL`
- `searchURL`
- `searchList`
- `searchName`
- `searchResult`
- `chapterRoads`
- `chapterResult`

Rules missing any required field must be skipped or migrated manually.

## Metadata mapping

| Kazumi field | Generated JS metadata |
| --- | --- |
| `name` | `@label`, plus `@key kazumi.{safeName}` |
| `version` | `@versionName`, computed `@versionCode` |
| `baseURL` | `@cover {baseURL}/favicon.ico` |

`safeName` is lowercase, replaces non-alphanumeric runs with `-`, and trims
leading/trailing `-`.

## Runtime mapping

| Kazumi field | Generated JS constant |
| --- | --- |
| `baseURL` | `BASE_URL` |
| `searchURL` | `SEARCH_URL` |
| `searchList` | `SEARCH_LIST_XPATH` |
| `searchName` | `SEARCH_NAME_XPATH` |
| `searchResult` | `SEARCH_RESULT_XPATH` |
| `chapterRoads` | `CHAPTER_ROADS_XPATH` |
| `chapterResult` | `CHAPTER_RESULT_XPATH` |
| `userAgent` | `USER_AGENT` |
| `referer` | `REFERER` |
| `usePost` | `USE_POST` |
| `useLegacyParser` | `USE_LEGACY_PARSER` |

XPath expressions are evaluated through `XPathUtils`, which wraps
`JsoupXpath` for Rhino use.

## Identity strategy

Kazumi rules usually do not provide a separate business id extraction rule.
The generated source therefore uses stable URLs as ids:

- `CartoonCover.id`: URL-encoded detail URL
- `Cartoon.url`: decoded detail URL
- `Episode.id`: URL-encoded episode page URL

This keeps search, detail, and play independent from site-specific URL
parsing.

## Cover strategy

Search covers are extracted from each search result item with
`XPathUtils.firstImage`.

Detail covers use the same helper on the detail document. The helper checks:

- `meta[property=og:image]`
- `meta[name=og:image]`
- `meta[name=twitter:image]`
- first `img` tag with common lazy-load attributes

All relative image URLs are normalized with `SourceUtils.urlParser`.

## Playback strategy

The generated play function decodes the episode page URL and calls:

```js
renderHelper.renderVideoFromJs(new JsVideoStrategy(...))
```

The Java `JsVideoStrategy` and `JsVideoResult` types avoid exposing
Kotlin-only types directly to Rhino. `RenderHelperImpl` converts them to the
internal Kotlin render types before executing the existing WebView sniffing
logic.

## Rhino compatibility

Generated JS uses only conservative syntax:

- `var`
- normal functions
- explicit `if` blocks
- Java collection classes such as `ArrayList` and `HashMap`

Generated JS avoids:

- ternary expressions
- `let` and `const`
- arrow functions
- template strings
- optional chaining

