package com.heyanle.easybangumi4.plugin.source.js

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
class JsTestProvider {

    companion object {
        val testJs = """
// @key heyanle.catwcy
// @label 鍠电墿娆″厓
// @versionName 1.0
// @versionCode 1
// @libVersion 11
// @cover https://www.catwcy.com/upload/site/20241103-1/eec404ebd39ac2f18800d8c0d914457e.png

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("棣栭〉", MainTab.MAIN_TAB_GROUP));
    res.add(new MainTab("鎺掓湡", MainTab.MAIN_TAB_GROUP));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    if (mainTab.label == "棣栭〉") {
        res.add(new SubTab("棣栭〉", true));
        res.add(new SubTab("TV鐣墽", true));
        res.add(new SubTab("鍓у満鐢靛奖", true));
    } else if (mainTab.label == "鎺掓湡") {
        res.add(new SubTab("鍛ㄤ竴", true));
        res.add(new SubTab("鍛ㄤ簩", true));
        res.add(new SubTab("鍛ㄤ笁", true));
        res.add(new SubTab("鍛ㄥ洓", true));
        res.add(new SubTab("鍛ㄤ簲", true));
        res.add(new SubTab("鍛ㄥ叚", true));
        res.add(new SubTab("鍛ㄦ棩", true));
    }
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {

    if (mainTab.label == "棣栭〉") {
        if (subTab.label == "棣栭〉") {
            var doc = getMainHomeDocument();
            return coverHomeMainCartoonCover(doc);
        }
     
    }
    return new Pair(null, new ArrayList());
}

// Hook DetailedComponent ========================================

// function DetailedComponent_getDetailed(summary) {
//     var cartoon = getCartoonDetailById(summary.id);
//     var playLine = getPlayLineById(summary.id);
//     return new Pair(cartoon, playLine);
// }


// utils ========================================
var lastDoc = null;
var lastDocTime = 0;
function getMainHomeDocument() {
    var now = System.currentTimeMillis();
    // 浜斿垎閽熺紦瀛?
    if (lastDoc == null || now - lastDocTime > 1000 * 60 * 5) {
        var ua = NetworkHelper.randomUA
        lastDoc = Jsoup.connect("https://www.catwcy.com/").userAgent(ua).get();
        lastDocTime = now;
    }
    return lastDoc;
}

function coverHomeMainCartoonCover(doc){
    var homeCenter = doc.select("div.slide-a.slide-c.rel div.slide-time-list.mySwiper div.swiper-wrapper").first()
    if (homeCenter == null) {
        throw new ParserException("瑙ｆ瀽閿欒");
    }
    var res = new ArrayList();
    var children = homeCenter.children();
    for (var i = 0; i < children.size(); i++) {
        var item = children.get(i);
        if (item == null) {
            continue;
        }
        var title = item.select("a div h3").text();
        var url =  JSSourceUtils.urlParser("https://www.catwcy.com", item.select("a").attr("href"));
        var array = url.split("/");
        var lastIndex = array.length - 1;
        var id = array[lastIndex];
        if (id.endsWith(".html")) {
            id = id.substring(0, id.length - 5);
        }
        var coverPattern = new Regex("(?<=url\().*(?=\))");
        var coverStyle = item.select("a div.slide-time-img3").attr("style");
        var cover = coverPattern.find(coverStyle, 0).groupValues[0];
        if (cover.startsWith("'")) {
            cover = cover.substring(1);
        }
        if (cover.endsWith("'")) {
            cover = cover.substring(0, cover.length - 1);
        }


        var url = element.select("a").attr("href");
        var coverUrl = element.select("img").attr("data-src");
        var title = element.select("img").attr("alt");
        var id = getCartoonId(url);
        res.add(makeCartoonCover({
            id: id,
            url: url,
            title: title,
            cover: coverUrl,
            intro: "",
        }));
        
    }
    return new Pair(null, res);



}


        """.trim()
    }
}