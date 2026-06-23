# Rhino JS Source Spec

本文件记录纯纯看番内置 Rhino JS 源的编写约束和 API 用法。当前运行时使用 `app/libs/rhino-1.7.15.jar`，`JSRuntime` 使用解释模式 `optimizationLevel = -1`。

## 1. 元数据

JS 源文件头部使用注释声明元数据，内置源放在 `app/src/main/assets/inner_source/`，文件后缀为 `.js`。

必须字段：

```js
// @key kazumi.NAME
// @label NAME
// @versionName 1.0
// @versionCode 100
// @libVersion 15
// @cover https://example.com/favicon.ico
```

- `key`：源唯一标识，必须稳定，不随站点域名变化轻易修改。
- `label`：展示名称。
- `versionName`：展示版本。
- `versionCode`：数字版本，升级源时递增。
- `libVersion`：必须在当前 `PluginV3.SUPPORTED_LIB_VERSION_RANGE` 内。
- `cover`：源图标 URL，可使用站点 favicon。

## 2. 语法约束

Rhino 1.7.15 对现代 JS 支持有限。源文件优先使用接近 ES5 的同步写法。社区源中已验证可运行的写法可以保留；如果本规范与已验证社区源冲突，以实际可运行源为准，并同步更新本文档。

支持写法：

```js
var list = new ArrayList();
function parse(doc) {
    return doc.text();
}
```

强制约束：

- 变量声明只能使用 `var`，不支持 `let` 和 `const`。
- 不使用箭头函数、模板字符串、解构、rest/spread、async/await、class、optional chaining、nullish coalescing。
- 播放页渲染嗅探使用 `renderHelper.renderVideoFromJs(...)`，不要再使用旧的 `webViewHelperV2.renderHtmlFromJs(...)` 手动解析播放地址。

推荐约束：

- 新写源建议语句末尾写 `;`。
- 新写源建议调用 Java/Kotlin 构造器时使用 `new`，例如 `new ArrayList()`、`new HashMap()`、`new Pair(...)`、`new PlayLine(...)`、`new Episode(...)`。
- 新写源建议抛异常时使用 `new ParserException("message")`。
- 工具 API 中名为 `xxxRegex` 的参数是传给客户端层处理的字符串；社区源里这类字符串规则可继续使用。
- 需要匹配字符串时，优先使用 `String.indexOf`、`split`、`substring` 等字符串 API；社区源已验证的正则相关写法可以保留。

禁止写法：

```js
let id = "1";
const url = "";
var m = /\/detail\/(.+)/.exec(url);
throw ParserException("parse failed");
var pair = Pair(null, list);
```

推荐写法：

```js
var id = "1";
var pair = new Pair(null, list);
throw new ParserException("parse failed");
```

不要使用：

- 箭头函数：`() => {}`
- 默认参数：`function f(a = 1) {}`
- 模板字符串：`` `${value}` ``
- 解构赋值
- rest/spread：`...items`
- async/await
- Promise 作为源核心流程
- class 语法
- optional chaining：`obj?.field`
- nullish coalescing：`a ?? b`

## 3. 注入对象

`JSComponentBundle` 会在加载源之前注入工具实例，注入名格式为 `Inject_${ClassSimpleName}`。源文件顶部应绑定成短变量：

```js
var networkHelper = Inject_NetworkHelper;
var okhttpHelper = Inject_OkhttpHelper;
var preferenceHelper = Inject_PreferenceHelper;
var renderHelper = Inject_RenderHelper;
var captchaHelper = Inject_CaptchaHelper;
var webProxyProvider = Inject_WebProxyProvider;
```

常用注入对象：

- `Inject_Source`：当前源对象，提前绑定为 `source`。
- `Inject_NetworkHelper`：UA、Cookie 工具。
- `Inject_OkhttpHelper`：OkHttp 客户端。
- `Inject_PreferenceHelper`：源配置读写。
- `Inject_RenderHelper`：WebView 渲染和播放嗅探。
- `Inject_CaptchaHelper`：图片验证码输入。
- `Inject_WebViewHelperV2`：底层 WebView helper，一般不直接使用。
- `Inject_WebProxyProvider`：创建 JS 可用 WebView 代理。
- `Inject_StringHelper`：字符串工具，当前源内较少直接使用。

不要把接口类型当实例使用：

```js
OkhttpHelper.client;
NetworkHelper.defaultLinuxUA;
RenderHelper.renderVideoFromJs(strategy);
WebProxyProvider.getWebProxy();
```

应该使用注入实例：

```js
okhttpHelper.client;
networkHelper.defaultLinuxUA;
renderHelper.renderVideoFromJs(strategy);
webProxyProvider.getWebProxy();
```

## 4. 工具 API

### 日志

```js
Log.i("TAG", "message");
Log.d("TAG", "message");
Log.w("TAG", "message");
Log.e("TAG", "message");
Log.v("TAG", "message");
```

### URL

```js
var absolute = SourceUtils.urlParser("https://example.com", "/detail/1");
```

### HTTP

`OkhttpUtils` 是 Java facade，Rhino 源应通过它创建请求：

```js
var request = OkhttpUtils.get(url);
var response = okhttpHelper.client.newCall(request).execute();
try {
    if (!response.isSuccessful()) {
        throw new ParserException("request failed: " + response.code());
    }
    return Jsoup.parse(response.body().string(), url);
} finally {
    response.close();
}
```

可用函数：

- `OkhttpUtils.get(url)`
- `OkhttpUtils.get(url, headerMap)`
- `OkhttpUtils.post(url)`
- `OkhttpUtils.postFromBody(url, formBodyMap)`

客户端：

- `okhttpHelper.client`：普通请求。
- `okhttpHelper.cloudflareClient`：Cloudflare OkHttp 客户端，不要默认使用。
- `okhttpHelper.cloudflareWebViewClient`：旧 WebView 验证客户端，JS 源不要直接使用；需要验证时使用 `webProxyProvider` 并抛验证请求。

### Jsoup

```js
var doc = Jsoup.parse(html, url);
var items = doc.select(".item");
var first = items.isEmpty() ? null : items.first();
```

### Preference

```js
var host = preferenceHelper.get("Host", "https://example.com");
preferenceHelper.put("Host", host);
var all = preferenceHelper.map();
```

### WebProxy

创建动态页面代理：

```js
var webProxy = webProxyProvider.getWebProxy();
webProxy.loadUrl(url, networkHelper.defaultLinuxUA, new HashMap(), null, false);
webProxy.waitingForPageLoaded(15000);
var html = webProxy.getContent(15000);
webProxy.close();
```

可用函数：

- `webProxy.loadUrl(url, userAgent, headers, interceptResRegex, needBlob)`
- `webProxy.waitingForPageLoaded(timeout)`
- `webProxy.waitingForResourceLoaded(resourceRegex, sticky, timeout)`
- `webProxy.href(url, cleanLoaded)`
- `webProxy.getContent(timeout)`
- `webProxy.getContentWithIframe(timeout)`
- `webProxy.waitingForBlobText(urlRegex, textRegex, sticky, timeout)`
- `webProxy.executeJavaScript(script, delay)`
- `webProxy.delay(delay)`
- `webProxy.addToWindow(show)`
- `webProxy.close()`
- `webProxy.needUserCheck()`
- `webProxy.needUserCheck(tips)`

`needUserCheck` 会抛内部验证异常，业务组件会将其包装为验证请求交给 UI。

### RenderHelper

JS 中只能使用非 suspend 桥接方法：

```js
var htmlResult = renderHelper.renderHtmlFromJs(new JsRenderedStrategy(
    url,
    "",
    "utf-8",
    networkHelper.defaultLinuxUA,
    new HashMap(),
    null,
    false,
    8000,
    false
));
```

播放嗅探：

```js
var result = renderHelper.renderVideoFromJs(new JsVideoStrategy(
    url,
    networkHelper.defaultLinuxUA,
    new HashMap(),
    null,
    15000,
    false
));
if (!result.url || String(result.url).length === 0) {
    throw new ParserException("play url parse failed");
}
return new PlayerInfo(
    result.isM3u8 ? PlayerInfo.DECODE_TYPE_HLS : PlayerInfo.DECODE_TYPE_OTHER,
    result.url
);
```

不要从 JS 调用 `renderHelper.renderVideo(...)` 或 `renderHelper.renderedHtml(...)`，它们是 suspend API。

### Captcha

图片验证码可用：

```js
var input = captchaHelper.start(image, "请输入验证码", "验证码", "验证码");
```

验证码优先级：源格式里 captcha 流程优先于 WebView verification。需要用户在 WebView 中处理时，使用 `webProxy.needUserCheck(...)`。

### 数据构造

封面：

```js
var cover = makeCartoonCover({
    id: id,
    source: source.key,
    url: url,
    title: title,
    cover: coverUrl,
    intro: intro
});
```

详情：

```js
var cartoon = makeCartoon({
    id: id,
    source: source.key,
    url: url,
    title: title,
    cover: coverUrl,
    intro: intro,
    description: description,
    genre: "TV, 连载",
    status: Cartoon.STATUS_UNKNOWN,
    updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS
});
```

播放线路：

```js
var episodes = new ArrayList();
episodes.add(new Episode(episodeId, episodeLabel, episodeOrder));
var line = new PlayLine(lineId, lineLabel, episodes);
```

播放信息：

```js
var playerInfo = new PlayerInfo(PlayerInfo.DECODE_TYPE_HLS, playUrl);
playerInfo.header = new HashMap();
```

建模约束：

- `CartoonCover.id` / `Cartoon.id` 使用稳定业务 id。
- `PlayLine.id` 使用线路业务 id。
- `PlayLine.label` 使用页面显示线路名称，不要删除 `VIP` 等页面前缀。
- `Episode.id` 使用分集业务 id。
- 不要把完整播放 URL 塞进 `Episode.id`；播放 URL 应在 `PlayComponent_getPlayInfo(summary, playLine, episode)` 中用三层数据拼接。

## 5. 引擎提前注入的包与函数

源执行前会先执行 `JsSource.JS_IMPORT`。

提前 import 的包：

```js
importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.runtime);
importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.entity);
importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js);
importPackage(Packages.com.heyanle.easybangumi4.plugin.api);
importPackage(Packages.com.heyanle.easybangumi4.plugin.api.utils.api);
importPackage(Packages.com.heyanle.easybangumi4.plugin.api.entity);
importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.utils);
importPackage(Packages.com.heyanle.easybangumi4.plugin.api.component.preference);
importPackage(Packages.kotlin.text);
importPackage(Packages.kotlin);
importPackage(Packages.java.util);
importPackage(Packages.java.lang);
importPackage(Packages.java.net);
importPackage(Packages.org.jsoup);
importPackage(Packages.org.json);
importPackage(Packages.okhttp3);
importPackage(Packages.javax.crypto);
```

提前注入变量：

```js
var Log = JSLogUtils;
var SourceUtils = JSSourceUtils;
var source = Inject_Source;
```

提前注入函数：

```js
function makeCartoonCover(map) {
    return SourceV3Bridge.makeCartoonCover(Inject_Source.key, map);
}

function makeCartoon(map) {
    return SourceV3Bridge.makeCartoon(Inject_Source.key, map);
}
```

## 6. 各个业务组件 API

组件函数必须同步返回对应类型。没有实现对应函数时，该组件视为不存在。

### PreferenceComponent

函数：

```js
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    res.add(new SourcePreference.Edit("网页", "Host", "https://example.com"));
    res.add(new SourcePreference.Switch("启用选项", "Enable", true));

    var selections = new ArrayList();
    selections.add("A");
    selections.add("B");
    res.add(new SourcePreference.Selection("线路", "Line", "A", selections));
    return res;
}
```

返回：`ArrayList<SourcePreference>`。

### PageComponent

函数：

```js
function PageComponent_getMainTabs() {
    var tabs = new ArrayList();
    tabs.add(new MainTab("最近更新", MainTab.MAIN_TAB_WITH_COVER, "update"));
    tabs.add(new MainTab("排行榜", MainTab.MAIN_TAB_WITHOUT_COVER, "rank"));
    tabs.add(new MainTab("分类", MainTab.MAIN_TAB_GROUP, "category"));
    return tabs;
}

function PageComponent_getSubTabs(mainTab) {
    var tabs = new ArrayList();
    if (String(mainTab.ext) === "category") {
        tabs.add(new SubTab("TV", true, "tv"));
        tabs.add(new SubTab("剧场版", true, "movie"));
    }
    return tabs;
}

function PageComponent_getContent(mainTab, subTab, key) {
    var list = new ArrayList();
    return new Pair(null, list);
}
```

返回约束：

- `getMainTabs()` 返回 `ArrayList<MainTab>` 或 `NonLabelMainTab`。
- `getSubTabs(mainTab)` 返回 `ArrayList<SubTab>`。
- `getContent(mainTab, subTab, key)` 返回 `new Pair(nextKey, ArrayList<CartoonCover>)`。
- `nextKey` 为下一页 key；没有下一页返回 `null`。

Tab 类型：

- `MainTab.MAIN_TAB_GROUP`
- `MainTab.MAIN_TAB_WITH_COVER`
- `MainTab.MAIN_TAB_WITHOUT_COVER`

### SearchComponent

普通搜索：

```js
function SearchComponent_search(pageKey, keyword) {
    var list = new ArrayList();
    return new Pair(null, list);
}
```

需要 WebView 验证：

```js
function SearchComponent_search(pageKey, keyword) {
    var webProxy = webProxyProvider.getWebProxy();
    webProxy.loadUrl(url, networkHelper.defaultLinuxUA, new HashMap(), null, false);
    webProxy.waitingForPageLoaded(15000);
    var html = webProxy.getContentWithIframe(15000);
    var doc = Jsoup.parse(String(html), url);
    if (!doc.select("button.verify-submit").isEmpty()) {
        webProxy.needUserCheck("请完成验证，出现搜索结果后返回");
    }
    return new Pair(null, parseSearch(doc));
}

function SearchComponent_searchWithCheck(pageKey, keyword, webProxy) {
    webProxy.waitingForPageLoaded(15000);
    var html = webProxy.getContentWithIframe(15000);
    return new Pair(null, parseSearch(Jsoup.parse(String(html), "")));
}
```

返回：`new Pair(nextPageKey, ArrayList<CartoonCover>)`。

### DetailedComponent

函数：

```js
function DetailedComponent_getDetailed(summary) {
    var cartoon = makeCartoon({
        id: String(summary.id),
        source: summary.source,
        url: "",
        title: "",
        cover: null,
        description: "",
        status: Cartoon.STATUS_UNKNOWN
    });

    var lines = new ArrayList();
    return new Pair(cartoon, lines);
}
```

参数：

- `summary.id`
- `summary.source`

返回：`new Pair(Cartoon, ArrayList<PlayLine>)`。

### PlayComponent

普通播放：

```js
function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var playUrl = "";
    return new PlayerInfo(PlayerInfo.DECODE_TYPE_OTHER, playUrl);
}
```

播放嗅探：

```js
function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var pageUrl = "";
    var result = renderHelper.renderVideoFromJs(new JsVideoStrategy(
        pageUrl,
        networkHelper.defaultLinuxUA,
        new HashMap(),
        null,
        15000,
        false
    ));
    if (!result.url || String(result.url).length === 0) {
        throw new ParserException("play url parse failed");
    }
    return new PlayerInfo(
        result.isM3u8 ? PlayerInfo.DECODE_TYPE_HLS : PlayerInfo.DECODE_TYPE_OTHER,
        result.url
    );
}
```

需要 WebView 验证：

```js
function PlayComponent_getPlayInfoWithCheck(summary, playLine, episode, webProxy) {
    webProxy.waitingForPageLoaded(15000);
    var html = webProxy.getContentWithIframe(15000);
    return parsePlayerInfo(html);
}
```

返回：`PlayerInfo`。

参数可用数据：

- `summary.id`
- `summary.source`
- `playLine.id`
- `playLine.label`
- `episode.id`
- `episode.label`
- `episode.order`

### 验证门禁

修改 JS 源、Rhino runtime 或本规范后至少运行：

```sh
./gradlew :app:testDebugUnitTest --tests "com.heyanle.easybangumi4.plugin.source.InnerJsSourceAssetTest" -Pksp.incremental=false
./gradlew :app:compileDebugKotlin -Pksp.incremental=false
```
