// @key heyanle.catwcy
// @label 喵物次元
// @versionName 1.0
// @versionCode 1
// @libVersion 11
// @cover https://www.catwcy.com/upload/site/20241103-1/eec404ebd39ac2f18800d8c0d914457e.png

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;

// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "Host", "https://www.catwcy.com");
    var playerUrl = new SourcePreference.Edit("播放器网页正则", "PlayerReg", "https://player.catw.moe/player/ec.php.*");
    res.add(host);
    res.add(playerUrl);
    return res;
}

// Hook PageComment  ========================================
var weekLabel = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("首页", MainTab.MAIN_TAB_GROUP));
    res.add(new MainTab("排期", MainTab.MAIN_TAB_GROUP));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    if (mainTab.label == "首页") {
        res.add(new SubTab("首页", true));
        res.add(new SubTab("TV番剧", true));
        res.add(new SubTab("剧场电影", true));
    } else if (mainTab.label == "排期") {
        for (var i = 0 ; i < weekLabel.length ; i ++){
            res.add(new SubTab(weekLabel[i], true, i));
        }
    }
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
    var doc = getMainHomeDocument();
    if (mainTab.label == "首页") {
        if (subTab.label == "首页") {
            return new Pair(null, coverHomeMainCartoonCover(doc));
        }
    } else if (mainTab.label == "排期") {
        var timeLine = coverTimeLine(doc);
        if (timeLine.size() != 7) {
            return new Pair(null, new ArrayList());
        }
        return new Pair(null, timeLine.get(subTab.ext));
    }
    return new Pair(null, new ArrayList());
}

// Hook DetailedComponent ========================================

 function DetailedComponent_getDetailed(summary) {
    var doc = getDetailDocument(summary);
    JSLogUtils.d("doc", doc);
    var cartoon = coverDetailCartoon(summary, summary.url, doc);
    var playLine = coverPlayLine(summary, summary.url, doc);

    return new Pair(cartoon, playLine);
 }

 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {

    var url = getHost() + "/search/page/" + page + "/wd/" + URLEncoder.encode(keyword, "utf-8") +".html";
    var ua = networkHelper.randomUA;
    var doc = Jsoup.connect(url).userAgent(ua).get();
    var res = new ArrayList();

    var children = doc.select("body div.box-width div.wrap div.public-list-box.search-box");
    for (var i = 0 ; i < children.size() ; i ++) {
        var child = children.get(i);
        if (child == null) {
            continue;
        }

        var title = child.select("div.thumb-content div.thumb-txt").text();
        var cover = child.select("a img").attr("data-src");
        var url = JSSourceUtils.urlParser(getHost(), child.select("a").first().attr("href"));
        var id = url2id(url);
        var intro = child.select("a div.public-list-prb").text();

        res.add(makeCartoonCover({
            id: id,
            url: url,
            title: title,
            cover: cover,
            intro: intro,
        }));
    }
    if (res.size() == 0) {
        return new Pair(null, new ArrayList());
    }




    return new Pair(page + 1, res);

 }

 // Hook PlayComponent ========================================
function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var url = JSSourceUtils.urlParser(getHost(), episode.id);
    var strategy = new WebViewHelperV2.RenderedStrategy(
        url,
        preferenceHelper.get("PlayerReg", "https://player.catw.moe/player/ec.php.*"),
        "utf-8",
        networkHelper.randomUA,
        null,
        null,
        false,
        5000
    );
    var result = webViewHelperV2.renderHtmlFromJs(strategy);
    if (result == null) {
        throw new ParserException("解析错误 1");
    }
    var playerUrl = result.interceptResource;
    if (playerUrl == null || playerUrl.length() == 0) {
        throw new ParserException("解析错误 2");
    }

    JSLogUtils.d("playerUrl", playerUrl);
    var urlIndex = playerUrl.indexOf("url=") + 4;
    var contentUrl = playerUrl.substring(urlIndex);
    if (contentUrl.startsWith("http")) {
        var type = PlayerInfo.DECODE_TYPE_OTHER;
        if (contentUrl.endsWith(".m3u8")) {
            type = PlayerInfo.DECODE_TYPE_HLS;
        } else if (contentUrl.endsWith(".mp4")) {
            type = PlayerInfo.DECODE_TYPE_OTHER;
        }

        return new PlayerInfo(type, contentUrl);
    }


    var strategy = new WebViewHelperV2.RenderedStrategy(
        playerUrl,
        ".*(\.mp4|\.m3u8).*",
        "utf-8",
        networkHelper.randomUA,
        null,
        null,
        false,
        5000
    );
    result = webViewHelperV2.renderHtmlFromJs(strategy);
    if (result == null) {
        throw new ParserException("解析错误 3");
    }
    contentUrl = result.interceptResource;
    var type = PlayerInfo.DECODE_TYPE_OTHER;
    if (contentUrl.endsWith(".m3u8")) {
        type = PlayerInfo.DECODE_TYPE_HLS;
    } else if (contentUrl.endsWith(".mp4")) {
        type = PlayerInfo.DECODE_TYPE_OTHER;
    }

    return new PlayerInfo(type, contentUrl);



}


// business ========================================
var rootHost = "https://www.catwcy.com"
function getHost() {
    return preferenceHelper.get("Host", rootHost);
}

// main

var lastDoc = null;
var lastDocTime = 0;
function getMainHomeDocument() {
    var now = System.currentTimeMillis();
    // 10s 缓存
    if (lastDoc == null || now - lastDocTime > 1000 * 10) {
        var ua = networkHelper.randomUA
        lastDoc = Jsoup.connect(getHost()).userAgent(ua).get();
        lastDocTime = now;
    }
    return lastDoc;
}

var lastTimeLineDoc = null;
var lastTimeLine = null;
// return Pair<Null, ArrayList<CartoonCover>>
function coverHomeMainCartoonCover(doc){
    // 按照 doc 缓存
    if (lastTimeLineDoc == doc && lastTimeLine != null) {
        return lastTimeLine;
    }

    var homeCenter = doc.select("div.slide-a.slide-c.rel div.slide-time-list.mySwiper div.swiper-wrapper").first();
    if (homeCenter == null) {
        throw new ParserException("解析错误");
    }
    var res = new ArrayList();
    var children = homeCenter.children();
    for (var i = 0; i < children.size(); i++) {
        var item = children.get(i);
        if (item == null) {
            continue;
        }
        var title = item.select("a div h3").text();
        var url =  JSSourceUtils.urlParser(getHost(), item.select("a").attr("href"));
        var id = url2id(url);

        var coverStyle = item.select("a div.slide-time-img3").attr("style");
        var cover = coverStyle;
        var start = coverStyle.indexOf("url(");
        if(start != -1){
            var end = coverStyle.indexOf(")", start);
            if(end != -1){
                cover = coverStyle.substring(start+4, end);
                if(cover.startsWith("'") || cover.startsWith("\"")){
                    cover = cover.substring(1, cover.length()-1);
                }
            }
        }
        res.add(makeCartoonCover({
            id: id,
            url: url,
            title: title,
            cover: cover,
            intro: "",
        }));

    }
    lastTimeLineDoc = doc;
    lastTimeLine = res;
    return res;
}

// return ArrayList<ArrayList<CartoonCover>>
function coverTimeLine(doc){
    var weekContainer = doc.select("div#week-module-box").first();
    if (weekContainer == null) {
        throw new ParserException("解析错误");
    }
    var hasData = false;
    var res = new ArrayList();
    for (var i = 1 ; i <= 7 ; i ++) {
        var dayContainer = weekContainer.select("div#week-module-" + i).first();
        if (dayContainer == null) {
            res.add(new ArrayList());
            continue;
        }
        var bangumiItemList = dayContainer.select("div.public-list-box");
        var dayRes = new ArrayList();
        for (var j = 0 ; j < bangumiItemList.size() ; j ++ ){
            var bangumiContainer = bangumiItemList.get(j);
            if (bangumiContainer == null) {
                continue;
            }
            // title url id cover
            var a = bangumiContainer.select("div.public-list-button a").first();
            if (a == null) {
                continue;
            }
            var sourceUrl = a.attr("href");
            var title = a.text();
            var url = JSSourceUtils.urlParser(getHost(), sourceUrl);
            var id = url2id(url);
            var cover = "";
            var imgContainer = bangumiContainer.select("a img").first();
            if (imgContainer != null) {
                cover = imgContainer.attr("data-src");
            }
            var intro = "";

            var subTitleContainer = bangumiContainer.select("div.public-list-button div.public-list-subtitle").first();
            if (subTitleContainer != null) {
                intro = subTitleContainer.text();
            }
            dayRes.add(
                makeCartoonCover({
                    id: id,
                    url: url,
                    title: title,
                    cover: cover,
                    intro: intro,
                })
            )
            hasData = true;
        }
        res.add(dayRes);
    }
    return res;
}

function getDetailDocument(summary) {
    var url = getHost() + "/bangumi/" + summary.id + ".html";
    var ua = networkHelper.randomUA;
    JSLogUtils.d("url", url);
    return Jsoup.connect(url).userAgent(ua).get();
}

// detail

// return CartoonImpl
function coverDetailCartoon(summary, url, doc) {
    // id, source, url, title, genre, coverUrl, intro, description, updateStrategy, isUpdate, status
    var descContainer = doc.select("div#height_limit").first();
    var desc = "";
    if (descContainer != null) {
        desc = descContainer.text().replace(" &nbsp", " ");
    }
    var title = doc.select("body  div.vod-detail div.box-width.flex div.left.flex div.detail-info  h3").first();
    var titleStr = "";
    if (title != null) {
        titleStr = title.text();
    }
    var coverContainer = doc.select("body div.vod-detail div img").first();
    var cover = "";
    if (coverContainer != null) {
        cover = coverContainer.attr("data-src");
    }
    JSLogUtils.d("cover", cover);
    var intro = "";
    var genreList = new ArrayList();
    var genreSet = new HashSet();
    var genreContainer = doc.select("body div.vod-detail.style-detail.rel.box.cor1 div.box-width.flex.between.rel div.left.flex div.detail-info.rel.wow a.deployment span");
    if (genreContainer != null) {
        for (var i = 0 ; i < genreContainer.size() ; i ++) {
            var genreItem = genreContainer.get(i).text();
            JSLogUtils.d("genreItem", genreItem);
            if (genreItem == null || genreItem.length == 0 || genreItem.equals(".") || genreItem.equals("·")) {
                continue;
            }
            var sp = genreItem.split(" ");
            for (var j = 0 ; j < sp.length ; j ++) {
                var item = sp[j];
                if (item.length == 0) {
                    continue;
                }
                if (genreSet.contains(item)) {
                    continue;
                }
                genreSet.add(item);
                genreList.add(item);
            }
            genreList.add(genreItem);
        }
    }


    var status = Cartoon.STATUS_UNKNOWN;
    var updateStrategy = Cartoon.UPDATE_STRATEGY_ALWAYS;
    return makeCartoon(
        {
            id: summary.id,
            url: url,
            title: titleStr,
            genreList: genreList,
            cover: cover,
            intro: intro,
            description: desc,
            updateStrategy: updateStrategy,
            isUpdate: false,
            status: status,
        }
    );


}

// return ArrayList<PlayLine>
function coverPlayLine(summary, url, doc) {
    var res = new ArrayList();
    var playLineContainer = doc.select("div div.anthology-tab").first();
    var episodeContainer = doc.select("div.anthology-list.select-a div.anthology-list-box.none ul.anthology-list-play");

    var playLineItems = playLineContainer.select("a.swiper-slide");
    for (var i = 0 ; i < playLineItems.size() ; i ++) {
        var playLineItem = playLineItems.get(i);
        var title = playLineItem.text();
        var playLine = new PlayLine(title, title, new ArrayList());


        var episodeItems = episodeContainer.get(i).select("li");
        for (var j = 0 ; j < episodeItems.size() ; j ++) {
            var episodeItem = episodeItems.get(j);
            var episodeTitle = episodeItem.text();
            var episodeUrl = episodeItem.select("a").attr("href");
            playLine.episode.add(new Episode(episodeUrl, episodeTitle, j));
        }
        res.add(playLine);
    }
    return res;
}


// utils
function url2id(url) {
    var array = url.split("/");
    var lastIndex = array.length - 1;
    var id = array[lastIndex];
    if (id.endsWith(".html")) {
        id = id.substring(0, id.length() - 5);
    }
    return id;
}

