// @key heyanle.ggl
// @label GiriGiriLove
// @versionName 1.2
// @versionCode 4
// @libVersion 12
// @cover https://bgm.girigirilove.com/upload/site/20231121-1/fdd2694db66628a9deadd86e50aedd43.png

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "HostV2", "https://bgm.girigirilove.com");
    var playerUrl = new SourcePreference.Edit("播放器网页正则", "PlayerReg", "https://m3u8.girigirilove.com/addons/aplyer/atom.php?.*");
    var timeout = new SourcePreference.Edit("超时时间", "Timeout", "10000");
    res.add(host);
    res.add(playerUrl);
    res.add(timeout);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("日番", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("美番", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("剧场版", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
//    var doc = getMainHomeDocument();
    if (mainTab.label == "日番") {
        var url = "/show/2--------" + (key+1) + "---/";
        var u = SourceUtils.urlParser(getRootUrl(), url);
        var res = getContent(u);
        if (res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "美番") {
        var url = "/show/3--------" + (key+1) + "---/";
        var u = SourceUtils.urlParser(getRootUrl(), url);
        var res = getContent(u);
        if (res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "剧场版") {
        var url = "/show/21--------" + (key+1) + "---/";
        var u = SourceUtils.urlParser(getRootUrl(), url);
        var res = getContent(u);
        if (res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    }
    return new Pair(null, new ArrayList());
}

function getContent(url) {
    var doc = getDoc(url);
    var list = new ArrayList();
    var elements = doc.select("div.border-box div.public-list-box");
//     Log.i("GiriGiriLove", "size: " + elements.size());
    for (var i = 0; i < elements.size() - 1; i++) {
        var it = elements.get(i);

        var uu = it.child(0).child(0).attr("href");
        var id = uu.subSequence(1, uu.length()-1).toString();

        var img = it.select("img").first()
        var coverUrl = "";
        if (img != null) {
            coverUrl = img.attr("data-src");
        }
        coverUrl = SourceUtils.urlParser(getRootUrl(), coverUrl);
        Log.i("GiriGiriLove", "coverUrl: " + coverUrl);

        var intro = it.select("span .public-list-prb").first();
        var introText = "";
        if (intro != null) {
            introText = intro.text();
        }
        list.add(
            makeCartoonCover({
               id: id,
               source: source.key,
               url: SourceUtils.urlParser(getRootUrl(), uu),
               title: it.child(1).child(0).text(),
               intro: introText,
               cover: coverUrl,
           })
        );

    }
    return list;


}

// Hook DetailedComponent ========================================

 function DetailedComponent_getDetailed(summary) {
    var u = SourceUtils.urlParser(getRootUrl(), summary.id);
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
    Log.i("GiriGiriLove", "detailed: " + title + ", cover: " + cover + ", desc: " + desc);
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


 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {

    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/search/" + URLEncoder.encode(keyword, "utf-8")+"----------" + (page+1) + "---/"
    )
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("div.box-width div.row-9 div div.search-box.public-list-box");
    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        var uu = it.child(1).child(0).attr("href")
        var id = uu.subSequence(1, uu.length() - 1).toString()

        var imgEle = it.select("img.gen-movie-img").first();
        var coverUrl = "";
        if (imgEle != null) {
            coverUrl = imgEle.attr("data-src");
        }
        var cover = coverUrl;
        if (cover.startsWith("//")) {
            cover = "http:${cover}"
        }
        var detailInfo = it.select("div.right").first();
        var titleEle = detailInfo.select("div.thumb-content div.thumb-txt").first();
        var title = "";
        if (titleEle != null) {
            title = titleEle.text();
        }
        var b = makeCartoonCover({
            id: id,
            title: title,
            url: SourceUtils.urlParser(getRootUrl(), uu),
            intro: "",
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

 // Hook PlayComponent ========================================
function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var urlPath = "";
    if (summary.id.startsWith("GV")) {
        urlPath = summary.id + "-" + playLine.id + "-" + episode.id;
    } else {
        urlPath = "GV"+ summary.id + "-" + playLine.id + "-" + episode.id;
    }
    var url = JSSourceUtils.urlParser(getRootUrl(), "play" + urlPath + "/");
    var strategy = new WebViewHelperV2.RenderedStrategy(
        url,
        preferenceHelper.get("PlayerReg", "https://m3u8.girigirilove.com/addons/aplyer/atom.php?.*"),
        "utf-8",
        networkHelper.defaultLinuxUA,
        null,
        null,
        false,
        Long.parseLong(preferenceHelper.get("Timeout", "10000"))
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
    Log.i("GiriGiriLove", "getDocFrom: " + url);
    var req = okhttpHelper.cloudflareWebViewClient.newCall(
        OkhttpUtils.get(u)
    );
    var string = req.execute().body().string();
    Log.i("GiriGiriLove", "getDoc: " + string);
    var doc = Jsoup.parse(string);
    return doc;
}

function getRootUrl() {
    return preferenceHelper.get("HostV2", "https://bgm.girigirilove.com");
}
