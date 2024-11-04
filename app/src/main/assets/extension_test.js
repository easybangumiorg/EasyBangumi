// @key heyanle.catwcy
// @label 喵物次元
// @versionName 1.0
// @versionCode 1
// @libVersion 11
// @cover https://www.catwcy.com/upload/site/20241103-1/eec404ebd39ac2f18800d8c0d914457e.png

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
     var cartoon = getCartoonDetailById(summary.id);
     var playLine = getPlayLineById(summary.id);
     return new Pair(cartoon, playLine);
 }

 // Hook SearchComponent ========================================

 // Hook PlayComponent ========================================


// business ========================================
var rootHost = "https://www.catwcy.com"

// main

var lastDoc = null;
var lastDocTime = 0;
function getMainHomeDocument() {
    var now = System.currentTimeMillis();
    // 10s 缓存
    if (lastDoc == null || now - lastDocTime > 1000 * 10) {
        var ua = NetworkHelper.randomUA
        lastDoc = Jsoup.connect(rootHost).userAgent(ua).get();
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
        var url =  JSSourceUtils.urlParser(rootHost, item.select("a").attr("href"));
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
            var url = JSSourceUtils.urlParser(rootHost, sourceUrl);
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
    var url = rootHost + "/" + summary.id + ".html";
    var ua = NetworkHelper.randomUA;
    return Jsoup.connect(rootHost).userAgent(ua).get();
}

// detail

// return CartoonImpl
function coverDetailCartoon(summary, doc) {
    // id, source, url, title, genre, coverUrl, intro, description, updateStrategy, isUpdate, status

}

// return ArrayList<PlayLine>
function coverPlayLine(summary, doc) {}


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

