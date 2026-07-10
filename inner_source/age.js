// @key heyanle.age
// @label Age动漫
// @versionName 1.0
// @versionCode 1
// @libVersion 15
// @cover https://www.agedm.io/favicon.ico

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var renderHelper = Inject_RenderHelper;
var okhttpHelper = Inject_OkhttpHelper;
var webProxyProvider = Inject_WebProxyProvider;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "Host", "www.agedm.io");
    res.add(host);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("今日推荐", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("更新时刻表", MainTab.MAIN_TAB_GROUP));
    return res;
}

var subTabTemp = new HashMap();

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    if (mainTab.label == "更新时刻表") {
        var url = "/update/";
        var u = SourceUtils.urlParser(getRootUrl(), url);
        var doc = getDoc(u);
    
        var elements = doc.select("#recent_update_video_wrapper > div.video_list_box");
        for (var i = 0; i < elements.size(); i++) {
             var list = new ArrayList();
            var it = elements.get(i);
            var tabLabel = it.child(0).select("button").text();

            var elementRootList = it.child(1).select("div.video_item");

            for (var j = 0; j < elementRootList.size(); j++) {
                var it = elementRootList.get(j);
                 var uu = it.child(1).child(0).attr("href");

                var spli = uu.split("/");
                var lss = spli.length;
                if (lss - 1 < 0) {
                    continue;
                }
            
                var id =  spli[lss - 1].toString();

                var img = it.select("img").first()
                var coverUrl = "";
                if (img != null) {
                    coverUrl = img.attr("data-original");
                }
                coverUrl = SourceUtils.urlParser(getRootUrl(), coverUrl);
                Log.i("GiriGiriLove", "coverUrl: " + coverUrl);

                var intro = it.select("span.video_item--info").first();
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
                }));
            }
            var key = tabLabel + System.currentTimeMillis();
            subTabTemp.put(key, list);

            res.add(new SubTab(tabLabel, true, key));
        }
    }
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
//    var doc = getMainHomeDocument();
    if (mainTab.label == "今日推荐") {
        var url = "/recommend/" + (key+1);
        var u = SourceUtils.urlParser(getRootUrl(), url);
        var res = getRecommendContent(u);
        if (res.size() == 0) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(key + 1, res);
    } else if (mainTab.label == "更新时刻表") {
        var res = subTabTemp.get(subTab.ext);
        return new Pair(null, res);
    } 
    return new Pair(null, new ArrayList());
}

function getRecommendContent(url) {
    var doc = getDoc(url);
    var list = new ArrayList();
    var elements = doc.select("#recommend_video_wrapper > div > div.video_list_box--bd > div div.video_item");
//     Log.i("GiriGiriLove", "size: " + elements.size());
    for (var i = 0; i < elements.size() - 1; i++) {
        var it = elements.get(i);

        var uu = it.child(1).child(0).attr("href");

        var spli = uu.split("/");
        var lss = spli.length;
        if (lss - 1 < 0) {
            continue;
        }
       
        var id =  spli[lss - 1].toString();

        var img = it.select("img").first()
        var coverUrl = "";
        if (img != null) {
            coverUrl = img.attr("data-original");
        }
        coverUrl = SourceUtils.urlParser(getRootUrl(), coverUrl);
        Log.i("GiriGiriLove", "coverUrl: " + coverUrl);

        var intro = it.select("span.video_item--info").first();
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
    var u = SourceUtils.urlParser(getRootUrl(), "/detail/" + summary.id);
    var doc = getDoc(u);
    JSLogUtils.d("doc", doc);
    var cartoon = detailed(doc, summary);
    var playLine = playline(doc, summary);
    return new Pair(cartoon, playLine);
 }

 function detailed(doc, summary) {
    var title = doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > h2").text()
//        var genre = doc.select("div.detail-info div.slide-info span").map { it.text() }.joinToString { ", " }
    var coverEle = doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_left > div.video_detail_cover > img").first();
    var cover =  "";
    if (coverEle != null) {
        cover = coverEle.attr("data-original");
    }
    var descEle = doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > div.video_detail_desc.py-2").first();
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
        doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > div.video_detail_playlist_wrapper.pt-4 > ul > li")
            .iterator();
    var epRoot = doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > div.video_detail_playlist_wrapper.pt-4 > div.tab-content > div > ul").iterator();
    var playLines = new ArrayList();
    var ii = 1;
    while (tabs.hasNext() && epRoot.hasNext()) {
        var tab = tabs.next();
        var ul = epRoot.next();

        var es = new ArrayList();
        var ulc = ul.children();

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


 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {

    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/search?query=" + URLEncoder.encode(keyword, "utf-8")+"&page=" + (page+1)
    )
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("#cata_video_list > div div.card.cata_video_item");
    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        var uu = it.select("a").first().attr("href");

         var spli = uu.split("/");
        var lss = spli.length;
        if (lss - 1 < 0) {
            continue;
        }
       
        var id =  spli[lss - 1].toString();

        var imgEle = it.select("img.video_thumbs").first();
        var coverUrl = "";
        if (imgEle != null) {
            coverUrl = imgEle.attr("data-original");
        }
        var cover = coverUrl;
        if (cover.startsWith("//")) {
            cover = "http:${cover}"
        }
        var detailInfo = it.select("div.card-body").first();
        var titleEle = detailInfo.select("h5.card-title").first();
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
    var url = JSSourceUtils.urlParser(getRootUrl(), "/play/" + summary.id + "/" + playLine.id + "/" + episode.id );
    return renderVideo(url, 45000, false);
}

function renderVideo(url, timeout, legacy) {
    var result = renderHelper.renderVideoFromJs(new JsVideoStrategy(
        url,
        networkHelper.defaultLinuxUA,
        new HashMap(),
        null,
        timeout,
        legacy
    ));
    var res = "";
    if (result != null) {
        res = result.url;
    }
    if (res == null || res.length == 0) {
        throw new ParserException("解析错误，未找到播放地址");
    }
    var type = PlayerInfo.DECODE_TYPE_OTHER;
    Log.i("AGE", result.isM3u8)
    if (result.isM3u8) {
        type = PlayerInfo.DECODE_TYPE_HLS;
    }
    return new PlayerInfo(type, res);
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
    return preferenceHelper.get("Host", "https://www.agedm.io");
}
