// @key heyanle.xifan
// @label 稀饭动漫
// @versionName 1.0
// @versionCode 1
// @libVersion 11
// @cover https://dm.xifanacg.com/template/dsn2/static/img/fav.png

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "Host", "https://dm.xifanacg.com");
    var playerUrl = new SourcePreference.Edit("播放器网页正则", "PlayerReg", "https://player.moedot.net/player/index.php?.*");
    var timeout = new SourcePreference.Edit("超时时间", "Timeout", "20000");
    res.add(host);
    res.add(playerUrl);
    res.add(timeout);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("连载新番", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("完结旧番", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("剧场版", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
//    var doc = getMainHomeDocument();
    if (mainTab.label == "连载新番") {
        var res = getContent(1, key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "完结旧番") {
        var res = getContent(2, key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "剧场版") {
        var res = getContent(3, key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    }
    return new Pair(null, new ArrayList());
}

function getContent(type, page) {
    var url = SourceUtils.urlParser(getRootUrl(), "/index.php/ds_api/vod");
    var map = new HashMap();
    map.put("type", type);
    map.put("page", page);
    map.put("level", 0);
    map.put("by", "time");
    var req = okhttpHelper.cloudflareWebViewClient.newCall(
        OkhttpUtils.postFromBody(url, map)
    );
    var string = req.execute().body().string();
    try {

        var object = new JSONObject(string);
        var list = object.getJSONArray("list");
        var res = new ArrayList();
        for (var i = 0; i < list.length(); i++) {
            var item = list.getJSONObject(i);
            var id = item.getString("vod_id");
            var title = item.getString("vod_name");
            var cover = item.getString("vod_pic");
            cover = SourceUtils.urlParser(getRootUrl(), cover);
            var url = SourceUtils.urlParser(getRootUrl(), item.getString("url"));
            var intro = item.getString("vod_blurb");
            if (intro == null) {
                intro = "";
            }
            res.add(makeCartoonCover({
                id: id,
                title: title,
                url: url,
                intro: intro,
                cover: SourceUtils.urlParser(getRootUrl(), cover),
                source: source.key,
            }));
        }
        return res;
    } catch(e) {
        Log.e("GiriGiriLove", "getContent: " + e);
        return new ArrayList();
    }
}
// Hook DetailedComponent ========================================

 function DetailedComponent_getDetailed(summary) {
    var u = SourceUtils.urlParser(getRootUrl(), "/bangumi/" + summary.id + ".html");
    var doc = getDoc(u);
    JSLogUtils.d("doc", doc);
    var cartoon = detailed(doc, summary);
    var playLine = playline(doc, summary);
    return new Pair(cartoon, playLine);
 }

 function detailed(doc, summary) {
    var title = doc.select("div.detail-info h3.slide-info-title").text()
//        var genre = doc.select("div.detail-info div.slide-info span").map { it.text() }.joinToString { ", " }
    var coverEle = doc.select("div.wow div.detail-pic img").first();
    var cover =  "";
    if (coverEle != null) {
        cover = coverEle.attr("data-src");
    }
    var descEle = doc.select("div.switch-box div.check div.text").first();
    var desc = "";
    if (descEle != null) {
        desc = descEle.text();
    }
    return makeCartoon({
        id: summary.id,
        url: SourceUtils.urlParser(getRootUrl(), summary.id),
        source: summary.source,
        title: title,
        cover: SourceUtils.urlParser(getRootUrl(), cover),
        intro: "",
        description: desc,
        genre: null,
        status: Cartoon.STATUS_UNKNOWN,
        updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS,
    });
 }
function playline(doc, summary) {
    var tabs =
        doc.select("div.anthology.wow div.anthology-tab div.swiper-wrapper a.swiper-slide")
            .iterator()
    var epRoot = doc.select("div.anthology-list-box div ul.anthology-list-play").iterator()
    var playLines = new ArrayList()
    var ii = 1
    while (tabs.hasNext() && epRoot.hasNext()) {
        var tab = tabs.next()
        var ul = epRoot.next()

        var es = new ArrayList()
        var ulc = ul.children()

        for (var index = 0; index < ulc.size(); index++) {
            var element = ulc.get(index)
            var label = "";
            if (element != null) {
                label = element.text();
            }
            es.add(
                new Episode(
                    id = (index + 1).toString(),
                    label = label,
                    order = index
                )
            )
        }
        playLines.add(
            new PlayLine(
                id = ii.toString(),
                label = tab.text(),
                episode = es
            )
        )
        ii++
    }
    return playLines
}
//
//
 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {

    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/search/wd/" + URLEncoder.encode(keyword, "utf-8")+"/page/" + (page+1) + ".html"
    )
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("div div.vod-detail.search-list");
    for (var i = 0; i < elements.size(); i++) {
        var par = elements.get(i);
        var it = par.child(0);
        var uu = it.child(1).child(0).attr("href")
        var id = uu.subSequence(9, uu.length() - 6).toString()

        var imgEle = it.select("img.gen-movie-img").first();
        var coverUrl = "";
        if (imgEle != null) {
            coverUrl = imgEle.attr("data-src");
        }
        var cover = coverUrl;
        if (cover.startsWith("//")) {
            cover = "http:${cover}"
        }
        var titleEle = it.select("h3.slide-info-title").first();
        var title = "";
        if (titleEle != null) {
            title = titleEle.text();
        }
        var intro = "";
        var introEle = it.select("span.slide-info-remarks").first();
        if (introEle != null) {
            intro = introEle.text();
        }
        var b = makeCartoonCover({
            id: id,
            title: title,
            url: SourceUtils.urlParser(getRootUrl(), uu),
            intro: intro,
            cover: SourceUtils.urlParser(getRootUrl(), cover),
            source: source.key,
        })
        res.add(b)
    }
    if (res.size() == 0) {
        return new Pair(null, new ArrayList());
    }
    return new Pair(page + 1, res);

 }
//
 // Hook PlayComponent ========================================
function PlayComponent_getPlayInfo(summary, playLine, episode) {

    var url = JSSourceUtils.urlParser(getRootUrl(), "/watch/" + summary.id + "/" + playLine.id + "/" + episode.id + ".html");
    var strategy = new WebViewHelperV2.RenderedStrategy(
        url,
        preferenceHelper.get("PlayerReg", "https://player.moedot.net/player/index.php?.*"),
        "utf-8",
        networkHelper.defaultLinuxUA,
        null,
        null,
        false,
        Long.parseLong(preferenceHelper.get("Timeout", "20000"))
    );
    var result = webViewHelperV2.renderHtmlFromJs(strategy);
    if (result == null) {
        throw new ParserException("解析错误 1");
    }
    Log.i("result", result);
    var doc = Jsoup.parse(result.content);


    var src = "";
    var iframe = doc.select("tbody td iframe").first();
    if (iframe != null) {
        src = iframe.attr("src")
    }
    Log.i("GiriGiriLove", "PlayComponent_getPlayInfo: src: " + src);
    var res = "";
    var split = src.split("\\?");
    if (split.length > 0) {
        var last = split[split.length - 1];
        var ls = last.split("\\&");
        for (var i = 0; i < ls.length; i++) {
            var it = ls[i];
            if (it.startsWith("url=")) {
                res = it.subSequence(4, it.length()).toString();
                break;
            }
        }
    }

    if(res.length == 0) {
        throw ParserException("url 解析失败")
    }

    var type = PlayerInfo.DECODE_TYPE_OTHER;
    if (res.endsWith(".m3u8")) {
        type = PlayerInfo.DECODE_TYPE_HLS;
    }
    return new PlayerInfo(
        type, res
    )


}


// business ========================================

// main
function getDoc(url) {
    var u = SourceUtils.urlParser(getRootUrl(), url);
    var req = okhttpHelper.cloudflareWebViewClient.newCall(
        OkhttpUtils.get(u)
    );
    var string = req.execute().body().string();
    Log.i("GiriGiriLove", "getDoc: " + string);
    var doc = Jsoup.parse(string);
    return doc;
}

function getRootUrl() {
    return preferenceHelper.get("Host", "https://dm.xifanacg.com");
}