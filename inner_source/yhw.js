// @key heyanle.yhw
// @label 樱花动漫
// @versionName 1.0
// @versionCode 1
// @libVersion 13
// @cover https://www.857yhw.com/favicon.ico

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
var webProxyProvider = Inject_WebProxyProvider;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "Host", "https://www.857yhw.com");
    var playerUrl = new SourcePreference.Edit("播放器网页正则", "PlayerReg", "https://danmu.yhdmjx.com/m3u8.php?.*");
    var timeout = new SourcePreference.Edit("超时时间", "Timeout", "20000");
    res.add(host);
    res.add(playerUrl);
    res.add(timeout);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("日本动漫", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("国产动漫", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("更新时刻表", MainTab.MAIN_TAB_GROUP));
    res.add(new MainTab("日本动漫电影", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("欧美动漫电影", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    if (mainTab.label == "更新时刻表") {
        var url = SourceUtils.urlParser(getRootUrl(), "");
        var doc = getDoc(url);

        var titles = doc.select("div.container > div > div > div > div > div.col-lg-wide-3.col-md-wide-25 > ul.stui-vodlist__text title > li").iterator();
        var contents = doc.select("div#content div.mod").iterator();
        while (titles.hasNext() && contents.hasNext()) {
            var title = titles.next();
            var content = contents.next();
            var label = title.text();
            var liList = content.select("ul.new_anime_page li");
            var r = new ArrayList();
            for (var i = 0; i < liList.size(); i++) {
                var it = liList.get(i);

                var a = it.select("a").first();
                var span = it.select("span").first();
                var uu = a.attr("href");
                var url = SourceUtils.urlParser(getRootUrl(), uu);
                var title = a.text();
                var intro = "";
                if (span != null) {
                    intro = span.text();
                }
                if (uu.length() < 13) {
                    continue;
                }
                var id = uu.subSequence(7, uu.length() - 5).toString();
                r.add(makeCartoonCover({
                    id: id,
                    title: title,
                    url: url,
                    intro: intro,
                    cover: "",
                }));
            }
            res.add(new SubTab(title, false, r));
        }
    }
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
    //    var doc = getMainHomeDocument();
    if (mainTab.label == "日本动漫") {
        var res = getContent("ribendongman", key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "国产动漫") {
        var res = getContent("guochandongman", key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "日本动漫电影") {
        var res = getContent("dongmandianying", key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "欧美动漫电影") {
        var res = getContent("oumeidongman", key + 1);
        if (res == null || res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
    } else if (mainTab.label == "更新时刻表") {
        var res = subTab.ext;
        return new Pair(null, res);
    }
    return new Pair(null, new ArrayList());
}

function getContent(type, page) {
    var url = SourceUtils.urlParser(getRootUrl(), "show/" + type + "--------" + page + "---.html");
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("div.container div div.myui-panel ul.myui-vodlist li");
    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        var a = it.select("a").first();
        var uu = a.attr("href");
        var url = SourceUtils.urlParser(getRootUrl(), uu);
        var cover = a.attr("data-original");
        if (uu.length() < 13) {
            continue;
        }
        var id = uu.subSequence(7, uu.length() - 5).toString();
        var title = a.attr("title");
        res.add(makeCartoonCover({
            id: id,
            title: title,
            url: url,
            intro: "",
            cover: SourceUtils.urlParser(getRootUrl(), cover),
            source: source.key,
        }));
    }
    return res;
}

// Hook DetailedComponent ========================================

function DetailedComponent_getDetailed(summary) {
    var u = SourceUtils.urlParser(getRootUrl(), "/video/" + summary.id + ".html");
    var doc = getDoc(u);
    var cartoon = detailed(doc, summary);
    var playLine = playline(doc, summary);
    return new Pair(cartoon, playLine);
}

function detailed(doc, summary) {
    var title = doc.select("body > div.container > div > div.col-md-wide-7.col-xs-1.padding-0 > div:nth-child(1) > div > div:nth-child(2) > div.myui-content__detail > h1").text()
    //        var genre = doc.select("div.detail-info div.slide-info span").map { it.text() }.joinToString { ", " }
    var coverEle = doc.select("body > div.container > div > div.col-md-wide-7.col-xs-1.padding-0 > div:nth-child(1) > div > div:nth-child(2) > div.myui-content__thumb > a > img").first();
    var cover = "";
    if (coverEle != null) {
        cover = coverEle.attr("data-original");
    }
    var descEle = doc.select("body > div.container > div > div.col-md-wide-7.col-xs-1.padding-0 > div:nth-child(1) > div > div:nth-child(2) > div.myui-content__detail > p.data.hidden-xs").first();
    var desc = "";
    if (descEle != null) {
        desc = descEle.ownText();
    }
    return makeCartoon({
        id: summary.id,
        url: SourceUtils.urlParser(getRootUrl(), "/video/" + summary.id + ".html"),
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
        doc.select("body > div.container > div > div.col-md-wide-7.col-xs-1.padding-0 > div:nth-child(4) > div > div.myui-panel_hd > div > ul li")
            .iterator();
    var epRoot = doc.select("body > div.container > div > div.col-md-wide-7.col-xs-1.padding-0 > div:nth-child(4) > div > div.tab-content.myui-panel_bd div").iterator();
    var playLines = new ArrayList();
    var ii = 1;
    while (tabs.hasNext() && epRoot.hasNext()) {
        var tab = tabs.next();
        var ul = epRoot.next();

        var es = new ArrayList();
        var ulc = ul.select("li");

        for (var index = 0; index < ulc.size(); index++) {
            var element = ulc.get(index);
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
            );
        }
        playLines.add(
            new PlayLine(
                id = ii.toString(),
                label = tab.text(),
                episode = es
            )
        );
        ii++;
    }
    return playLines;
}
//
//
// Hook SearchComponent ========================================
function SearchComponent_search(page, keyword) {

    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/search/" + URLEncoder.encode(keyword, "utf-8") + "----------" + (page + 1) + "---.html"
    )
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("ul#searchList li");
    for (var i = 0; i < elements.size(); i++) {
        var par = elements.get(i);
        var a = par.select("a").first();
        if (a == null) {
            Log.i("88dm", "SearchComponent_search: a is null");
            continue;
        }
        var uu = a.attr("href");
        if (uu.length() < 13) {
            Log.i("88dm", "SearchComponent_search: href length < 13");
            continue;
        }
        var id = uu.subSequence(7, uu.length() - 5).toString()
        var title = a.attr("title");
        var cover = a.attr("data-original");
        if (cover.startsWith("//")) {
            cover = "http:${cover}";
        }
        var b = makeCartoonCover({
            id: id,
            title: title,
            url: SourceUtils.urlParser(getRootUrl(), uu),
            intro: "",
            cover: SourceUtils.urlParser(getRootUrl(), cover),
            source: source.key,
        });
        res.add(b);
    }
    if (res.size() == 0) {
        return new Pair(null, new ArrayList());
    }
    return new Pair(page + 1, res);

}
//
// Hook PlayComponent ========================================
function PlayComponent_getPlayInfo(summary, playLine, episode) {

    var url = JSSourceUtils.urlParser(getRootUrl(), "/play/" + summary.id + "-" + playLine.id + "-" + episode.id + ".html");
    var webProxy = webProxyProvider.getWebProxy();
    if (webProxy == null) {
        throw new ParserException("解析错误 webProxy is null");
    }
    webProxy.loadUrl(url, networkHelper.defaultLinuxUA, null, null, true);
    //    webProxy.addToWindow(true);

    var res = webProxy.waitingForResourceLoaded(preferenceHelper.get("PlayerReg", "https://danmu.yhdmjx.com/m3u8.php?.*"));
    Log.i("88dm", "PlayComponent_getPlayInfo1: " + res);
    if (res != null && res.length() > 0) {
        webProxy.href(res);
        webProxy.waitingForPageLoaded();
    }
    var tt = 0;
    var video = null;
    var content = "";
    while (tt < 6) {
        webProxy.delay(500);
        content = webProxy.getContentWithIframe();
        var doc = Jsoup.parse(content);
        video = doc.select("#lelevideo").first();
        if (video != null) {
            break;
        } else {
            tt++;
        }
    }
    Log.i("88dm", "PlayComponent_getPlayInfo: " + content);
    if (video == null) {
        throw new ParserException("解析错误 2");
    }
    var src = video.attr("src");
    Log.i("88dm", "PlayComponent_getPlayInfo2: " + src);
    if (!src.startsWith("blob:")) {
        var type = PlayerInfo.DECODE_TYPE_HLS;
        if (src.contains("type=video_mp4")) {
            type = PlayerInfo.DECODE_TYPE_OTHER;
        }
        return new PlayerInfo(
            type, src
        )
    }
    src = webProxy.waitingForResourceLoaded(".*\\.m3u8", true, 10000);
    //    webProxy.addToWindow(true);
    if (src != null) {
        var type = PlayerInfo.DECODE_TYPE_HLS;
        if (src.contains("type=video_mp4")) {
            type = PlayerInfo.DECODE_TYPE_OTHER;
        }
        return new PlayerInfo(
            type, src
        )
    }
    throw new ParserException("解析错误 3");
}


// business ========================================
// main
function getDoc(url) {
    var u = SourceUtils.urlParser(getRootUrl(), url);
    var req = okhttpHelper.cloudflareWebViewClient.newCall(
        OkhttpUtils.get(u)
    );
    var string = req.execute().body().string();
    //    Log.i("88dm", "getDoc: " + string);
    var doc = Jsoup.parse(string);
    return doc;
}

function getRootUrl() {
    return preferenceHelper.get("Host", "https://www.857yhw.com");
}
