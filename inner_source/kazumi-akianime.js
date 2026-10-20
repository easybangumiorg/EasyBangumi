// @key kazumi.akianime
// @label akianime
// @versionName 1.3
// @versionCode 10300
// @libVersion 15
// @cover https://www.akianime.cc/favicon.ico

var networkHelper = Inject_NetworkHelper;
var renderHelper = Inject_RenderHelper;
var okhttpHelper = Inject_OkhttpHelper;
var preferenceHelper = Inject_PreferenceHelper;

var DEFAULT_BASE_URL = "https://www.akianime.cc/";
var SEARCH_URL = "https://www.akianime.cc/bgmsearch/-------------.html?wd=@keyword";
var SEARCH_LIST_XPATH = "//div[@class='vod-detail style-detail cor4 search-list']";
var SEARCH_NAME_XPATH = "//div/div[2]/a/h3";
var SEARCH_RESULT_XPATH = "//div/div[2]/div[5]/div[1]/a   ";
var CHAPTER_ROADS_XPATH = "//ul[@class='anthology-list-play size']";
var CHAPTER_RESULT_XPATH = "//li/a";
var USER_AGENT = "";
var REFERER = "";
var USE_POST = false;
var USE_LEGACY_PARSER = false;
var PLAY_TIMEOUT = 30000;

function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    res.add(new SourcePreference.Edit("网页", "Host", DEFAULT_BASE_URL));
    return res;
}


function SearchComponent_search(pageKey, keyword) {
    var url = runtimeUrl(SEARCH_URL).replace("@keyword", URLEncoder.encode(keyword, "utf-8"));
    var doc = getDoc(url);
    var items = XPathUtils.nodes(doc, SEARCH_LIST_XPATH);
    var list = new ArrayList();
    for (var i = 0; i < items.size(); i++) {
        var item = items.get(i);
        var title = XPathUtils.text(item, SEARCH_NAME_XPATH);
        var href = XPathUtils.attr(item, SEARCH_RESULT_XPATH, "href");
        if (title == null || String(title).trim().length == 0) {
            continue;
        }
        if (href == null || href.length == 0) {
            continue;
        }
        var detailUrl = absoluteUrl(href);
        var coverUrl = absoluteUrl(XPathUtils.firstImage(item));
        list.add(makeCartoonCover({
            id: encodeSourceId(detailUrl, title, coverUrl),
            source: source.key,
            url: detailUrl,
            title: title,
            intro: "",
            cover: coverUrl
        }));
    }
    return new Pair(null, list);
}

function DetailedComponent_getDetailed(summary) {
    var sourceId = decodeSourceId(String(summary.id));
    var detailUrl = runtimeUrl(sourceId.url);
    var doc = getDoc(detailUrl);
    var coverUrl = sourceId.cover;
    if (coverUrl == null || coverUrl.length == 0) {
        coverUrl = absoluteUrl(XPathUtils.firstImage(doc));
    }
    var title = sourceId.title;
    if (title == null || title.length == 0) {
        title = cleanTitle(XPathUtils.title(doc));
    }
    if (title == null || title.length == 0) {
        title = detailUrl;
    }
    var roads = XPathUtils.nodes(doc, CHAPTER_ROADS_XPATH);
    var lines = new ArrayList();
    for (var i = 0; i < roads.size(); i++) {
        var road = roads.get(i);
        var episodeNodes = XPathUtils.nodes(road, CHAPTER_RESULT_XPATH);
        var episodes = new ArrayList();
        for (var j = 0; j < episodeNodes.size(); j++) {
            var ep = episodeNodes.get(j);
            var epHref = XPathUtils.attrSelf(ep, "href");
            var epLabel = XPathUtils.textSelf(ep);
            if (epHref == null || epHref.length == 0) {
                continue;
            }
            var epUrl = absoluteUrl(epHref);
            episodes.add(new Episode(encodeSourceId(epUrl, "", ""), epLabel, j));
        }
        if (episodes.size() > 0) {
            lines.add(new PlayLine(String(i), "播放线路" + (i + 1), episodes));
        }
    }
    var cartoon = makeCartoon({
        id: summary.id,
        source: summary.source,
        url: detailUrl,
        title: title,
        cover: coverUrl,
        intro: "",
        description: "",
        status: Cartoon.STATUS_UNKNOWN,
        updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS
    });
    return new Pair(cartoon, lines);
}

function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var pageUrl = runtimeUrl(decodeSourceId(String(episode.id)).url);
    var directUrl = tryParseDirectPlayerUrl(pageUrl);
    if (directUrl != null && String(directUrl).length > 0) {
        return makePlayerInfo(directUrl, isM3u8Url(directUrl));
    }
    var result = renderHelper.renderVideoFromJs(new JsVideoStrategy(
        pageUrl,
        getUserAgent(),
        makeHeaders(),
        null,
        PLAY_TIMEOUT,
        USE_LEGACY_PARSER
    ));
    var res = "";
    var isM3u8 = false;
    if (result != null && result.url != null) {
        res = String(result.url);
        isM3u8 = result.isM3u8;
    }
    if (res == null || String(res).length == 0) {
        throw new ParserException(String("播放地址解析失败"), null);
    }
    return makePlayerInfo(res, isM3u8);
}

function makePlayerInfo(url, isM3u8) {
    var type = PlayerInfo.DECODE_TYPE_OTHER;
    if (isM3u8) {
        type = PlayerInfo.DECODE_TYPE_HLS;
    }
    var playerInfo = new PlayerInfo(type, String(url));
    playerInfo.header = makeHeaders();
    return playerInfo;
}

function tryParseDirectPlayerUrl(pageUrl) {
    try {
        var html = getText(pageUrl);
        return extractDirectVideoFromHtml(html, pageUrl);
    } catch (e) {
        return "";
    }
}

function extractDirectVideoFromHtml(html, pageUrl) {
    if (html == null || String(html).length == 0) {
        return "";
    }
    var match = String(html).match(/player_aaaa\s*=\s*\{[\s\S]*?"url"\s*:\s*"([^"]*)"/);
    if (match == null || match.length < 2) {
        return "";
    }
    var raw = String(match[1]).replace(/\\//g, "/").replace(/\u0026/g, "&");
    var resolved = SourceUtils.urlParser(String(pageUrl), raw);
    if (isDirectVideoUrl(resolved)) {
        return resolved;
    }
    return "";
}

function isDirectVideoUrl(url) {
    var lower = String(url == null ? "" : url).toLowerCase();
    return (lower.indexOf("http://") == 0 || lower.indexOf("https://") == 0 || lower.indexOf("//") == 0) &&
        (lower.indexOf(".m3u8") >= 0 || lower.indexOf(".mp4") >= 0);
}

function isM3u8Url(url) {
    return String(url == null ? "" : url).toLowerCase().indexOf(".m3u8") >= 0;
}

function getText(url) {
    var response = okhttpHelper.client.newCall(OkhttpUtils.get(url, makeHeaders())).execute();
    try {
        if (!response.isSuccessful()) {
            return "";
        }
        var body = response.body();
        if (body == null) {
            return "";
        }
        return body.string();
    } finally {
        response.close();
    }
}

function getDoc(url) {
    var request;
    if (USE_POST) {
        var uri = URI.create(url);
        var form = parseQueryToMap(uri.getRawQuery());
        var postUrl = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        request = OkhttpUtils.postFromBody(postUrl, form, makeObjectHeaders());
    } else {
        request = OkhttpUtils.get(url, makeHeaders());
    }
    var response = okhttpHelper.client.newCall(request).execute();
    try {
        if (!response.isSuccessful()) {
            throw new ParserException(String("请求失败: " + response.code()), null);
        }
        return Jsoup.parse(response.body().string(), url);
    } finally {
        response.close();
    }
}

function makeHeaders() {
    var headers = new HashMap();
    headers.put("user-agent", getUserAgent());
    if (REFERER != null && REFERER.length > 0) {
        headers.put("referer", runtimeUrl(REFERER));
    } else {
        headers.put("referer", getRootUrl());
    }
    return headers;
}

function makeObjectHeaders() {
    var headers = new HashMap();
    var stringHeaders = makeHeaders();
    var iterator = stringHeaders.entrySet().iterator();
    while (iterator.hasNext()) {
        var item = iterator.next();
        headers.put(item.getKey(), item.getValue());
    }
    return headers;
}

function getUserAgent() {
    if (USER_AGENT != null && USER_AGENT.length > 0) {
        return USER_AGENT;
    }
    return networkHelper.defaultLinuxUA;
}

function absoluteUrl(url) {
    return SourceUtils.urlParser(getRootUrl(), String(url));
}

function runtimeUrl(url) {
    if (url == null || String(url).length == 0) {
        return getRootUrl();
    }
    var parsed = String(url);
    var oldRoot = normalizeRootUrl(DEFAULT_BASE_URL);
    if (parsed.indexOf(oldRoot) == 0) {
        return getRootUrl() + parsed.substring(oldRoot.length);
    }
    return SourceUtils.urlParser(getRootUrl(), parsed);
}

function getRootUrl() {
    return normalizeRootUrl(preferenceHelper.get("Host", DEFAULT_BASE_URL));
}

function normalizeRootUrl(url) {
    var value = String(url == null ? "" : url).trim();
    if (value.length == 0) {
        value = DEFAULT_BASE_URL;
    }
    if (value.indexOf("http://") != 0 && value.indexOf("https://") != 0) {
        value = "https://" + value;
    }
    return value.replace(/\/+$/, "") + "/";
}

function cleanTitle(value) {
    if (value == null) {
        return "";
    }
    var text = String(value);
    var parts = text.split("-");
    if (parts.length > 0 && parts[0].length > 0) {
        text = parts[0];
    }
    text = text.replace("《", "");
    text = text.replace("》", "");
    return text.trim();
}

function encodeSourceId(url, title, cover) {
    return URLEncoder.encode(String(url), "utf-8") + "|" +
        URLEncoder.encode(String(title == null ? "" : title), "utf-8") + "|" +
        URLEncoder.encode(String(cover == null ? "" : cover), "utf-8");
}

function decodeSourceId(value) {
    var parts = String(value).split("|", 3);
    return {
        url: URLDecoder.decode(parts[0], "utf-8"),
        title: parts.length > 1 ? URLDecoder.decode(parts[1], "utf-8") : "",
        cover: parts.length > 2 ? URLDecoder.decode(parts[2], "utf-8") : ""
    };
}

function parseQueryToMap(query) {
    var map = new HashMap();
    if (query == null || query.length == 0) {
        return map;
    }
    var parts = String(query).split("&");
    for (var i = 0; i < parts.length; i++) {
        var kv = String(parts[i]).split("=");
        var key = URLDecoder.decode(kv[0], "utf-8");
        var value = "";
        if (kv.length > 1) {
            value = URLDecoder.decode(kv[1], "utf-8");
        }
        map.put(key, value);
    }
    return map;
}
