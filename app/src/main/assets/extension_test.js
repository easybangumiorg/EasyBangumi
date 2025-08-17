// @key heyanle.mxfan
// @label MX动漫
// @versionName 1.0
// @versionCode 1
// @libVersion 12
// @cover https://www.mxdm.xyz/mxstatic/picture/logo.png

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "Host", "https://www.mxdm.tv");
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
    res.add(new MainTab("首页", MainTab.MAIN_TAB_GROUP));
    res.add(new MainTab("更新时间表", MainTab.MAIN_TAB_WITHOUT_COVER));
    res.add(new MainTab("日本动漫", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("国产动漫", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("动漫电影", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("欧美电影", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    if (mainTab.label == "首页") {
        var url = SourceUtils.urlParser(getRootUrl(), "/");
        var doc = getDoc(url);
        var contents = doc.select(".content .module .module-list>.module-items").iterator();
        var titles = doc.select(".content .module .module-title").iterator();
        while (titles.hasNext() && contents.hasNext()) {
            var contentEl = contents.next();
            var titleEl = titles.next();
            var title = titleEl.text().trim();
            var videos = contentEl.select(".module-item")
            if (videos.isEmpty()) {
                continue
            }
            if (videos.get(0).classNames().size() > 1) {
                continue
            }
            res.add(new SubTab(title, true, contentEl));
        }
    } else if (mainTab.label == "更新时间表") {
        var url = SourceUtils.urlParser(getRootUrl(), "/");
        var doc = getDoc(url);
        var tabs = document.selectFirst(".mxoneweek-tabs");
        if (tabs == null) {
            return res;
        }
        var activeTabIndex = 0;

        var tabNames = new ArrayList();
        var tabsChildren = tabs.children();
        for (var i = 0; i < tabsChildren.size(); i++) {
            var el = tabsChildren.get(i);
            if (el.hasClass("active")) {
                activeTabIndex = index
            }
            tabNames.add(el.text().trim());
        }

        var videoGroups = new ArrayList();
        var listSele = document.select(".mxoneweek-list");
        for (var i = 0; i < listSele.size(); i++) {
            var el = listSele.get(i);
            var a = el.getElementsByTag("a");
            var cartoonList = new ArrayList();
            for (var j = 0; j < a.size(); j++) {
                var link = a.get(j);
                var title = "";
                if (link.childrenSize() > 0) {
                    title = link.child(0).text().trim();
                } else {
                    title = link.text().trim();
                }
                var episodeText = "";
                if (link.childrenSize() > 1) {
                    episodeText = link.child(1).text().trim();
                }
                var url = link.absUrl("href");
                var videoId = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
                cartoonList.add(makeCartoonCover({
                    id: videoId,
                    title: title,
                    url: getRootUrl() + "/dongman/" + videoId + ".html",
                    intro: episodeText,
                    source: source.key,
                }));
            }
            videoGroups.add(cartoonList);
        }
        var minSize = tabNames.size();
        if (videoGroups.size() < minSize) {
            minSize = videoGroups.size();
        }
        for (var i = activeTabIndex ;  i < videoGroups; i ++) {
            res.add(new SubTab(tabNames.get(i), false, videoGroups.get(i)));
        }
        for (var i = 0; i < activeTabIndex; i++) {
            res.add(new SubTab(tabNames.get(i), false, videoGroups.get(i)));
        }
    }
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
//    var doc = getMainHomeDocument();
    if (mainTab.label == "首页") {
        var contentEl = subTab.ext;
        if (contentEl == null) {
            return new Pair(null, new ArrayList());
        }
        var videos = contentEl.select(".module-item")
        if (videos.isEmpty()) {
           return new Pair(null, new ArrayList());
        }
        if (videos.get(0).classNames().size() > 1) {
            return new Pair(null, new ArrayList());
        }

        var cartoonList = new ArrayList();
        for (var i = 0; i < videos.size(); i++) {
            var videoEl = videos.get(i);
            cartoonList.add(parseToCartoonCover(videoEl));
        }
        return new Pair(null, cartoonList);
    } else if (mainTab.label == "更新时间表") {
        var res = subTab.ext;
        if (res == null) {
            return new Pair(null, new ArrayList());
        } else {
             return new Pair(null, res);
        }
    } else if (mainTab.label == "日本动漫") {
        return getContent("riman", key)
    } else if (mainTab.label == "国产动漫") {
        return getContent("guoman", key)
    } else if (mainTab.label == "动漫电影") {
        return getContent("dmdianying", key)
    } else if (mainTab.label == "欧美电影") {
        return getContent("oman", key)
    }
    return new Pair(null, new ArrayList());
}

function getContent(type, page){
    var url = SourceUtils.urlParser(getRootUrl(), "/show/" + type + "--------" + (page + 1) + "---.html");
    var doc = getDoc(url);

    var videoSelect = doc.select(".content .module .module-item");
    var res = new ArrayList();
    for (var i = 0; i < videoSelect.size(); i++) {
        var item = videoSelect.get(i);
        res.add(parseToCartoonCover(item));
    }
    var hasNext = hasNextPage(doc);
    var next = null;
    if (hasNext && !res.isEmpty()) {
        next = page + 1;
    }
    return new Pair(next, res);
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
        var id = uu.subSequence(9, uu.length() - 5).toString()
        Log.i("search id", id);

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

function hasNextPage(document) {
    var page = document.getElementById("page")
    if (page == null) {
        return false;
    }
    var currentPageIndex = 0;
    var children = page.children();
    for (var i = 0; i < children.size(); i++) {
        var child = children.get(i);
        if (child.hasClass("page-current")) {
            currentPageIndex = i;
            break;
        }
    }
    return currentPageIndex != -1 && currentPageIndex < page.childrenSize() - 3
}

function extractImageSrc(imageElement) {
     var img = imageElement.dataset().get("src");
        if (img == null) {
            img = "";
        }
        if (img.isEmpty() && imageElement.hasAttr("src")) {
            img = imageElement.attr("src");
        }
        
        return img;
}

function parseToCartoonCover(element) {
    var imgEle = element.selectFirst("img");
    var coverUrl = extractImageSrc(imgEle);
    var linkEl = element.selectFirst("a");
    var url = linkEl.absUrl("href");
    var id = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
    var videoTitle = element.selectFirst(".video-name").text().trim();
    var tags = new LinkedHashSet();

    var episode = "";
    var first = element.selectFirst(".module-item-text");
    if (first != null) {
        episode = first.text().trim();
    }
    if (!episode.isEmpty()) {
        tags.add(episode)
    }
    var childrenEle = element.selectFirst(".module-item-caption");
    if (childrenEle != null) {
        var children = childrenEle.children();
        for (var index = 0; index < children.size(); index++) {
            var child = children.get(index);
            var tag = child.text().trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
    }

    var stringBuilder = new stringBuilder();
    for (var i = 0 ; i < tags.size(); i++) {
        var tag = tags.get(i);
        // Do something with each tag if needed
        stringBuilder.append(tag);
        if (i < tags.size() - 1) {
            stringBuilder.append(" | ");
        }
    }

    return makeCartoonCover({
        id: id,
        title: videoTitle,
        url: SourceUtils.urlParser(getRootUrl(), url),
        intro: stringBuilder.toString(),
        cover: coverUrl,
        source: source.key,
    });
}

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
