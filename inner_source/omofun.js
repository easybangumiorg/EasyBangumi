// @key jisoft.omofun
// @label oMo
// @versionName 1.3
// @versionCode 3
// @libVersion 12
// @cover https://omofun.link/upload/mxprocms/20230707-1/49f4b8a7fd5cbf77ffcfa7a52e755675.gif

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "HostV2", "https://omofun.link");
    var timeout = new SourcePreference.Edit("超时时间", "Timeout", "10000");
    res.add(host);
    res.add(timeout);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("动漫", MainTab.MAIN_TAB_WITH_COVER));
//    res.add(new MainTab("电影", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("连续剧", MainTab.MAIN_TAB_WITH_COVER));

//    res.add(new MainTab("里番", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
    // 分类ID映射表 - 遵循DRY原则，避免重复的if-else
    var categoryIds = {
        "电影": 1,
        "剧集": 2,
        "动漫": 4,
        "里番": 5
    };

    var categoryId = categoryIds[mainTab.label];
    if (!categoryId) {
        return new Pair(null, new ArrayList());
    }

    var url = "/vod/show/by/hits/id/"+categoryId+"/page/"+(key + 1)+".html";

    var u = SourceUtils.urlParser(getRootUrl(), url);

    var res = getContent(u);

    return res.size() == 0 ?
        new Pair(null, new ArrayList()) :
        new Pair(key + 1, res);
}


function getContent(url) {
    var doc = getDoc(url);
    var list = new ArrayList();
    var elements = doc.select(".module-poster-item.module-item");

    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        var uu = it.attr("href");
        var id = uu.substring(uu.lastIndexOf("/") + 1, uu.lastIndexOf("."));

        var img = it.select("div.module-item-pic img").first();
        var coverUrl = img.attr("data-original");
        Log.d("getContent", "coverUrl: " + coverUrl);

        var title = it.select("div.module-poster-item-title").text() || it.attr("title");
        var intro = it.select("div.module-item-note").text();

        list.add(
            makeCartoonCover({
               id: id,
               source: source.key,
               url: uu,
               title: title,
               intro: intro,
               cover: coverUrl,
           })
        );
    }
    return list;
}

// Hook DetailedComponent ========================================

 function DetailedComponent_getDetailed(summary) {
    var u = SourceUtils.urlParser(getRootUrl(), "/vod/detail/id/"+summary.id);
    var doc = getDoc(u);
    JSLogUtils.d("doc", doc);
    var cartoon = detailed(doc, summary);
    var playLine = playline(doc, summary);
    return new Pair(cartoon, playLine);
 }

 function detailed(doc, summary) {
    var title = doc.select("div.module-info-heading h1").text()
//        var genre = doc.select("div.detail-info div.slide-info span").map { it.text() }.joinToString { ", " }
    var coverEle = doc.select("div.module-item-pic img").first();
    var cover =  "";
    if (coverEle != null) {
        cover = coverEle.attr("data-original");
    }
    var descEle = doc.select("div.show-desc p").first();
    var desc = "";
    if (descEle != null) {
        desc = descEle.text();
    }

    return makeCartoon({
        id: summary.id,
        url: SourceUtils.urlParser(getRootUrl(), "/vod/detail/id/"+summary.id),
        source: summary.source,
        title: title,
        cover: cover,
        intro: "",
        description: desc,
        genre: null,
        status: Cartoon.STATUS_UNKNOWN,
        updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS,
    });
 }
function playline(doc, summary) {
    // 获取所有播放线路标签
    var tabItems = doc.select("div.module-tab-item.tab-item")
    // 获取所有播放列表容器
    var playListContainers = doc.select("div.module-list.sort-list.tab-list.his-tab-list")

    var playLines = new ArrayList()
    var totalLines = tabItems.size() // 获取总线路数

    // 遍历每个播放线路
    for (var i = 0; i < tabItems.size(); i++) {
        var tabItem = tabItems.get(i)
        var playListContainer = playListContainers.get(i)

        // 获取线路名称
        var tabSpan = tabItem.select("span").first()
        var lineName = ""
        if (tabSpan != null) {
            lineName = tabSpan.text()
        }

        // 获取该线路下的所有集数链接
        var episodeLinks = playListContainer.select("a.module-play-list-link")
        var episodes = new ArrayList()
        // 使用递减的ID：第一个线路ID最大，依次递减
        var playLinesId = (totalLines - i).toString()
        Log.d("playline", "线路 " + (i+1) + " 名称: " + lineName + ", 分配ID: " + playLinesId)

        // 解析每个集数
        for (var j = 0; j < episodeLinks.size(); j++) {
            var link = episodeLinks.get(j)
            var episodeSpan = link.select("span").first()
            var episodeLabel = ""
            if (episodeSpan != null) {
                episodeLabel = episodeSpan.text()
            }


            episodes.add(
                new Episode(
                    id = (j + 1).toString(),
                    label = episodeLabel,
                    order = j
                )
            )
        }

        // 创建播放线路对象
        playLines.add(
            new PlayLine(
                id = playLinesId,
                label = lineName,
                episode = episodes
            )
        )
    }

    return playLines
}


 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {

    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/vod/search/page/" + (page+1) + "/wd/" + URLEncoder.encode(keyword, "utf-8") + ".html"
    )
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("div.module-card-item.module-item");
    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        var uu = it.select("a.module-card-item-poster").attr("href");
        var id = uu.substring(uu.lastIndexOf("/") + 1, uu.lastIndexOf("."));

        var imgEle = it.select("div.module-item-pic img").first();
        var coverUrl = "";
        if (imgEle != null) {
            coverUrl = imgEle.attr("data-original");
        }

        var titleEle = it.select("div.module-card-item-title strong").first();
        var title = "";
        if (titleEle != null) {
            title = titleEle.text();
        }

        var introEle = it.select("div.module-info-item-content").last();
        var intro = "";
        if (introEle != null) {
            intro = introEle.text();
        }

        var b = makeCartoonCover({
            id: id,
            title: title,
            url: uu,
            intro: intro,
            cover: coverUrl,
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
    var url = JSSourceUtils.urlParser(getRootUrl(), "/vod/play/id/"+summary.id+"/sid/"+playLine.id+"/nid/"+episode.id+".html");

   var doc = getDoc(url);
    var res = "";
    // 匹配 var player_aaaa 对象中的 url 字段
    var urlPattern = /var player_aaaa=\{.*?"url":"([^"]+)".*\}/;
    var urlMatch = doc.toString().match(urlPattern);

    if (urlMatch != null && urlMatch.length > 1) {
        res = urlMatch[1];
        // 去掉URL中的转义斜杠
        res = res.replace(/\\\//g, "/");
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

    var doc = Jsoup.parse(string);
    return doc;
}

function getRootUrl() {
    return preferenceHelper.get("HostV2", "https://omofun.link");
}
