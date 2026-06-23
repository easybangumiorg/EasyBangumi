// @key kazumi.mengfan
// @label mengfan
// @versionName 1.2
// @versionCode 10200
// @libVersion 15
// @cover https://www.mfan.tv/favicon.ico

var networkHelper = Inject_NetworkHelper;
var renderHelper = Inject_RenderHelper;
var okhttpHelper = Inject_OkhttpHelper;

var BASE_URL = "https://www.mfan.tv/";
var SEARCH_URL = "https://www.mfan.tv/search/?wd=@keyword&submit=";
var SEARCH_LIST_XPATH = "//div/div[2]/div/div[2]/div/div[2]/div[1]/div/div[2]/div/ul/li";
var SEARCH_NAME_XPATH = "//div/div/div[2]/div[1]/div/a";
var SEARCH_RESULT_XPATH = "//div/div/div[2]/div[1]/div/a";
var CHAPTER_ROADS_XPATH = "//div[2]/div[2]/div/div/div[2]/div/div[1]/div[2]//div[position() > 1]";
var CHAPTER_RESULT_XPATH = "//li/a";
var USER_AGENT = "";
var REFERER = "";
var USE_POST = false;
var USE_LEGACY_PARSER = false;
var PLAY_TIMEOUT = 30000;

function SearchComponent_search(pageKey, keyword) {
    var url = SEARCH_URL.replace("@keyword", URLEncoder.encode(keyword, "utf-8"));
    var doc = getDoc(url);
    var items = XPathUtils.nodes(doc, SEARCH_LIST_XPATH);
    var list = new ArrayList();
    for (var i = 0; i < items.size(); i++) {
        var item = items.get(i);
        var title = XPathUtils.text(item, SEARCH_NAME_XPATH);
        var href = XPathUtils.attr(item, SEARCH_RESULT_XPATH, "href");
        if (href == null || href.length == 0) {
            continue;
        }
        var detailUrl = absoluteUrl(href);
        var coverUrl = absoluteUrl(XPathUtils.firstImage(item));
        list.add(makeCartoonCover({
            id: encodeSourceId(detailUrl),
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
    var detailUrl = decodeSourceId(String(summary.id));
    var doc = getDoc(detailUrl);
    var coverUrl = absoluteUrl(XPathUtils.firstImage(doc));
    var title = XPathUtils.title(doc);
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
            episodes.add(new Episode(encodeSourceId(epUrl), epLabel, j));
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
    var pageUrl = decodeSourceId(String(episode.id));
    var result = renderHelper.renderVideoFromJs(new JsVideoStrategy(
        pageUrl,
        getUserAgent(),
        makeHeaders(),
        null,
        PLAY_TIMEOUT,
        USE_LEGACY_PARSER
    ));
    var res = "";
    if (result != null) {
        res = result.url;
    }
    if (res == null || res.length == 0) {
        throw new ParserException("播放地址解析失败");
    }
    var type = PlayerInfo.DECODE_TYPE_OTHER;
    if (result.isM3u8) {
        type = PlayerInfo.DECODE_TYPE_HLS;
    }
    var playerInfo = new PlayerInfo(type, res);
    playerInfo.header = makeHeaders();
    return playerInfo;
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
            throw new ParserException("请求失败: " + response.code());
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
        headers.put("referer", REFERER);
    } else {
        headers.put("referer", BASE_URL);
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
    return SourceUtils.urlParser(BASE_URL, String(url));
}

function encodeSourceId(value) {
    return URLEncoder.encode(String(value), "utf-8");
}

function decodeSourceId(value) {
    return URLDecoder.decode(String(value), "utf-8");
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
