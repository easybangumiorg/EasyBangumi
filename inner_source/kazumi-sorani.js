// @key kazumi.sorani
// @label sorani
// @versionName 1.0
// @versionCode 10000
// @libVersion 15
// @cover https://www.sorani.net/favicon.ico

var networkHelper = Inject_NetworkHelper;
var renderHelper = Inject_RenderHelper;
var okhttpHelper = Inject_OkhttpHelper;
var preferenceHelper = Inject_PreferenceHelper;

var DEFAULT_BASE_URL = "https://www.sorani.net/";
var SEARCH_API_URL = "https://api.sorani.cc/sorani-cms/api/video";
var SEARCH_API_QUERY = new JSONObject("{\"page\":1,\"size\":50,\"keyword\":\"@keyword\",\"enabled\":true,\"sortMode\":\"latest\",\"sortDesc\":false}");
var SEARCH_LIST_PATH = "$.data.records[*]";
var SEARCH_NAME_PATH = "$.title";
var SEARCH_SOURCE_PATH = "$.id";
var CHAPTER_API_URL = "https://api.sorani.cc/sorani-cms/api/video/@source";
var CHAPTER_API_QUERY = new JSONObject("{}");
var CHAPTER_ROADS_PATH = "$.data";
var ROAD_NAME_PATH = "";
var EPISODES_PATH = "$.episodes[*]";
var EPISODE_NAME_PATH = "$.episodeLabel";
var EPISODE_URL_PATH = "$.episodeOrder";
var EPISODE_PAGE_URL = "https://www.sorani.net/anime/mal/@source/episode/@episodeUrl";
var EPISODE_PAGE_QUERY = new JSONObject("{}");
var CHAPTER_VARIABLES = new JSONObject("{}");
var USER_AGENT = "";
var REFERER = "https://www.sorani.net/";
var USE_POST = false;
var USE_LEGACY_PARSER = false;
var PLAY_TIMEOUT = 30000;

function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    res.add(new SourcePreference.Edit("网页", "Host", DEFAULT_BASE_URL));
    return res;
}

function PageComponent_getMainTabs() {
    return new NonLabelMainTab(MainTab.MAIN_TAB_WITH_COVER);
}

function PageComponent_getSubTabs(mainTab) {
    return new ArrayList();
}

function PageComponent_getContent(mainTab, subTab, pageKey) {
    if (pageKey != null && String(pageKey) != "0") {
        return new Pair(null, new ArrayList());
    }
    return new Pair(null, getHomeContent());
}

function getHomeContent() {
    var doc = getDoc(getRootUrl());
    var nodes = findHomeNodes(doc);
    var list = new ArrayList();
    var seen = new HashMap();
    for (var i = 0; i < nodes.size(); i++) {
        var item = nodes.get(i);
        var href = homeHref(item);
        var title = cleanTitle(homeTitle(item));
        var coverUrl = absoluteUrl(XPathUtils.firstImage(item));
        if (!isHomeHref(href) || isBadHomeTitle(title) || !isLikelyCoverUrl(coverUrl)) {
            continue;
        }
        var detailUrl = absoluteUrl(href);
        var uniqueKey = normalizeHomeUrl(detailUrl);
        if (seen.containsKey(uniqueKey)) {
            continue;
        }
        seen.put(uniqueKey, true);
        list.add(makeCartoonCover({
            id: encodeSourceId(detailUrl, title, coverUrl),
            source: source.key,
            url: detailUrl,
            title: title,
            intro: homeIntro(item),
            cover: coverUrl
        }));
        if (list.size() >= 40) {
            break;
        }
    }
    return list;
}

function findHomeNodes(doc) {
    var selectors = [
        "a.module-poster-item[href]",
        "a.module-item[href]",
        "a.module-card-item[href]",
        ".stui-vodlist__box",
        ".stui-vodlist li",
        ".public-list-box",
        "div.main ul li",
        "ul li"
    ];
    for (var i = 0; i < selectors.length; i++) {
        var nodes = doc.select(selectors[i]);
        if (nodes != null && nodes.size() >= 6) {
            return nodes;
        }
    }
    return doc.select("a[href]");
}

function homeHref(item) {
    var direct = trimText(item.attr("href"));
    if (isHomeHref(direct)) {
        return direct;
    }
    var selectors = ["a[href]", "h3 a[href]", "h2 a[href]", ".txt a[href]"];
    for (var i = 0; i < selectors.length; i++) {
        var link = item.select(selectors[i]).first();
        if (link == null) {
            continue;
        }
        var href = trimText(link.attr("href"));
        if (isHomeHref(href)) {
            return href;
        }
    }
    return "";
}

function homeTitle(item) {
    var selectors = [
        ".module-poster-item-title",
        ".module-card-item-title",
        ".module-item-title",
        ".video-info-header h3",
        "h3",
        "h2",
        ".txt a",
        "strong"
    ];
    for (var i = 0; i < selectors.length; i++) {
        var node = item.select(selectors[i]).first();
        if (node == null) {
            continue;
        }
        var title = trimText(node.text());
        if (title.length == 0) {
            title = trimText(node.attr("title"));
        }
        if (title.length > 0) {
            return title;
        }
    }
    var directTitle = trimText(item.attr("title"));
    if (directTitle.length > 0) {
        return directTitle;
    }
    var firstLink = item.select("a[href]").first();
    if (firstLink != null) {
        var linkTitle = trimText(firstLink.attr("title"));
        if (linkTitle.length > 0) {
            return linkTitle;
        }
        linkTitle = trimText(firstLink.text());
        if (linkTitle.length > 0) {
            return linkTitle;
        }
    }
    var firstImage = item.select("img").first();
    if (firstImage != null) {
        return trimText(firstImage.attr("alt"));
    }
    return "";
}

function homeIntro(item) {
    var selectors = [
        ".module-item-note",
        ".module-item-text",
        ".module-card-item-note",
        ".public-list-prb",
        ".remarks",
        "p"
    ];
    for (var i = 0; i < selectors.length; i++) {
        var node = item.select(selectors[i]).first();
        if (node == null) {
            continue;
        }
        var text = trimText(node.text());
        if (text.length > 0) {
            return text;
        }
    }
    return "";
}

function isHomeHref(href) {
    var value = trimText(href);
    if (value.length == 0 || value == "/" || value == "#") {
        return false;
    }
    var lower = value.toLowerCase();
    if (lower.indexOf("javascript:") == 0 || lower.indexOf("mailto:") == 0) {
        return false;
    }
    var blocked = [
        "/search",
        "/vodsearch",
        "/type/",
        "/vodtype/",
        "/label/",
        "/topic/",
        "/gbook",
        "/map",
        "/rss/",
        "/user/",
        "/login",
        "/register",
        ".xml"
    ];
    for (var i = 0; i < blocked.length; i++) {
        if (lower.indexOf(blocked[i]) >= 0) {
            return false;
        }
    }
    return true;
}

function trimText(value) {
    if (value == null) {
        return "";
    }
    return String(value).replace(/\s+/g, " ").trim();
}

function normalizeHomeUrl(url) {
    var value = trimText(url);
    var hashIndex = value.indexOf("#");
    if (hashIndex >= 0) {
        value = value.substring(0, hashIndex);
    }
    var queryIndex = value.indexOf("?");
    if (queryIndex >= 0) {
        value = value.substring(0, queryIndex);
    }
    while (value.length > 0 && value.charAt(value.length - 1) == "/") {
        value = value.substring(0, value.length - 1);
    }
    return value;
}

function isBadHomeTitle(title) {
    var value = trimText(title);
    if (value.length == 0) {
        return true;
    }
    var blocked = ["专题", "排行", "排行榜", "热榜", "目录", "APP下载", "安卓APP下载", "下载APP", "立即下载"];
    for (var i = 0; i < blocked.length; i++) {
        if (value.indexOf(blocked[i]) >= 0) {
            return true;
        }
    }
    return false;
}

function isLikelyCoverUrl(url) {
    var value = normalizeHomeUrl(url);
    if (value.length == 0) {
        return false;
    }
    var root = normalizeHomeUrl(getRootUrl());
    if (value == root) {
        return false;
    }
    return value.indexOf("http://") == 0 || value.indexOf("https://") == 0;
}

function SearchComponent_search(pageKey, keyword) {
    var variables = new HashMap();
    variables.put("keyword", keyword);
    variables.put("page", pageKey == null ? 1 : pageKey);
    var url = buildApiUrl(SEARCH_API_URL, SEARCH_API_QUERY, variables);
    var root = new JSONObject(getRequiredText(url));
    var items = jsonPathList(root, SEARCH_LIST_PATH);
    var list = new ArrayList();
    var seen = new HashMap();
    for (var i = 0; i < items.size(); i++) {
        var item = items.get(i);
        var title = jsonPathString(item, SEARCH_NAME_PATH);
        var remoteId = jsonPathString(item, SEARCH_SOURCE_PATH);
        if (title.length == 0 || remoteId.length == 0) {
            continue;
        }
        var coverUrl = apiImage(item);
        var id = encodeSourceId(remoteId, title, coverUrl);
        if (seen.containsKey(id)) {
            continue;
        }
        seen.put(id, true);
        list.add(makeCartoonCover({
            id: id,
            source: source.key,
            url: getRootUrl(),
            title: title,
            intro: apiDescription(item),
            cover: coverUrl
        }));
    }
    return new Pair(null, list);
}

function DetailedComponent_getDetailed(summary) {
    var sourceId = decodeSourceId(String(summary.id));
    var variables = new HashMap();
    variables.put("source", sourceId.url);
    var detailUrl = buildApiUrl(CHAPTER_API_URL, CHAPTER_API_QUERY, variables);
    var root = new JSONObject(getRequiredText(detailUrl));
    addConfiguredVariables(variables, root);
    var detail = jsonPathValue(root, "$.data");
    if (detail == null) {
        detail = root;
    }
    var title = apiFirstString(detail, ["name", "title"]);
    if (title.length == 0) {
        title = sourceId.title;
    }
    var coverUrl = apiImage(detail);
    if (coverUrl.length == 0) {
        coverUrl = sourceId.cover;
    }
    var roads = jsonPathList(root, CHAPTER_ROADS_PATH);
    var lines = new ArrayList();
    for (var i = 0; i < roads.size(); i++) {
        var road = roads.get(i);
        var roadName = jsonPathString(road, ROAD_NAME_PATH);
        if (roadName.length == 0) {
            roadName = "播放线路" + (i + 1);
        }
        var episodeNodes = jsonPathList(road, EPISODES_PATH);
        var episodes = new ArrayList();
        for (var j = 0; j < episodeNodes.size(); j++) {
            var episode = episodeNodes.get(j);
            var episodeName = jsonPathString(episode, EPISODE_NAME_PATH);
            var episodeValue = jsonPathString(episode, EPISODE_URL_PATH);
            variables.put("roadIndex", i);
            variables.put("episodeIndex", j);
            variables.put("episodeUrl", episodeValue);
            var episodePage = buildApiUrl(EPISODE_PAGE_URL, EPISODE_PAGE_QUERY, variables);
            episodes.add(new Episode(encodeSourceId(episodePage, "", ""), episodeName, j));
        }
        if (episodes.size() > 0) {
            lines.add(new PlayLine(String(i), roadName, episodes));
        }
    }
    var description = apiDescription(detail);
    var cartoon = makeCartoon({
        id: summary.id,
        source: summary.source,
        url: getRootUrl(),
        title: title,
        cover: coverUrl,
        intro: description,
        description: description,
        status: Cartoon.STATUS_UNKNOWN,
        updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS
    });
    return new Pair(cartoon, lines);
}

function addConfiguredVariables(variables, root) {
    var keys = CHAPTER_VARIABLES.keys();
    while (keys.hasNext()) {
        var key = String(keys.next());
        variables.put(key, jsonPathString(root, String(CHAPTER_VARIABLES.get(key))));
    }
}

function buildApiUrl(template, query, variables) {
    var url = runtimeUrl(expandTemplate(template, variables));
    var keys = query.keys();
    var separator = url.indexOf("?") >= 0 ? "&" : "?";
    while (keys.hasNext()) {
        var key = String(keys.next());
        var value = expandTemplate(String(query.get(key)), variables);
        url += separator + URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8");
        separator = "&";
    }
    return url;
}

function expandTemplate(template, variables) {
    var value = String(template);
    var iterator = variables.entrySet().iterator();
    while (iterator.hasNext()) {
        var entry = iterator.next();
        value = value.split("@" + entry.getKey()).join(String(entry.getValue()));
    }
    return value;
}

function jsonPathValue(root, path) {
    if (root == null) {
        return null;
    }
    var normalized = String(path == null ? "" : path).trim();
    if (normalized.length == 0 || normalized == "$") {
        return root;
    }
    if (normalized.indexOf("$.") == 0) {
        normalized = normalized.substring(2);
    }
    var parts = normalized.split(".");
    var value = root;
    for (var i = 0; i < parts.length; i++) {
        var key = String(parts[i]).replace(/\[\*\]$/, "");
        if (key.length == 0) {
            continue;
        }
        if (!(value instanceof JSONObject)) {
            return null;
        }
        value = value.opt(key);
        if (value == null || value == JSONObject.NULL) {
            return null;
        }
    }
    return value;
}

function jsonPathList(root, path) {
    var value = jsonPathValue(root, path);
    var result = new ArrayList();
    if (value instanceof JSONArray) {
        for (var i = 0; i < value.length(); i++) {
            var item = value.opt(i);
            if (item != null && item != JSONObject.NULL) {
                result.add(item);
            }
        }
    } else if (value != null) {
        result.add(value);
    }
    return result;
}

function jsonPathString(root, path) {
    if (path == null || String(path).trim().length == 0) {
        return "";
    }
    var value = jsonPathValue(root, path);
    if (value == null || value == JSONObject.NULL) {
        return "";
    }
    return String(value);
}

function apiFirstString(object, fields) {
    if (!(object instanceof JSONObject)) {
        return "";
    }
    for (var i = 0; i < fields.length; i++) {
        var value = object.opt(fields[i]);
        if (value != null && value != JSONObject.NULL && String(value).trim().length > 0) {
            return String(value).trim();
        }
    }
    return "";
}

function apiImage(object) {
    var value = apiFirstString(object, ["pic", "cover", "picThumb", "coverThumb", "image", "thumbnail"]);
    if (value.length == 0) {
        return "";
    }
    return absoluteUrl(value);
}

function apiDescription(object) {
    return apiFirstString(object, ["content", "summary", "blurb", "description", "remarks"]);
}

function getRequiredText(url) {
    var text = getText(url);
    if (text == null || String(text).length == 0) {
        throw new ParserException(String("API 请求失败: " + url), null);
    }
    return text;
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
    var raw = String(match[1]).replace(/\\\//g, "/").replace(/\u0026/g, "&");
    var resolved = SourceUtils.urlParser(String(pageUrl), raw);
    if (isDirectVideoUrl(resolved)) {
        return resolved;
    }
    return "";
}

function isDirectVideoUrl(url) {
    var lower = String(url == null ? "" : url).toLowerCase();
    var path = lower.split("?", 1)[0].split("#", 1)[0];
    var supportedScheme = path.indexOf("http://") == 0 ||
        path.indexOf("https://") == 0 || path.indexOf("//") == 0;
    return supportedScheme && (path.endsWith(".m3u8") || path.endsWith(".mp4"));
}

function isM3u8Url(url) {
    var lower = String(url == null ? "" : url).toLowerCase();
    var path = lower.split("?", 1)[0].split("#", 1)[0];
    return path.endsWith(".m3u8");
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
        var postUrl = uri.getScheme() + "://" + uri.getRawAuthority() + uri.getPath();
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
        var part = String(parts[i]);
        var equalsIndex = part.indexOf("=");
        var rawKey = equalsIndex >= 0 ? part.substring(0, equalsIndex) : part;
        var key = URLDecoder.decode(rawKey, "utf-8");
        var value = "";
        if (equalsIndex >= 0) {
            value = URLDecoder.decode(part.substring(equalsIndex + 1), "utf-8");
        }
        map.put(key, value);
    }
    return map;
}
