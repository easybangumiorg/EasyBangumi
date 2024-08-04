package com.heyanle.easybangumi4.plugin.js

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
class JsTestProvider {

    companion object {
        val testJs = """
// @key cyc
// @label 次元城 JS
// @versionName 1.0
// @versionCode 1
// @libVersion 11
// @cover https://www.cycanime.com/upload/site/20240319-1/67656e504da1f0c61513066dcea769fb.png

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("首页", MainTab.MAIN_TAB_GROUP));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    res.add(new SubTab("推荐", true));
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
    var elements = getRecomElement();
    var res = new ArrayList();
    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);
        res.add(createCartoonCover(it));
    }
    return new Pair(null, res);

}

function DetailedComponent_getDetailed(summary) {
    var cartoon = getCartoonDetailById(summary.id);
    var playLine = getPlayLineById(summary.id);
    return new Pair(cartoon, playLine);
}
var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0";

/**
 * 获取推荐番剧
 * @return Elements 推荐番剧
 */
function getRecomElement() {
    return Jsoup.connect("https://www.cycanime.com").userAgent(userAgent).get().getElementsByClass("swiper-wrapper diy-center")[0].getElementsByClass("public-list-exp");
}

function createCartoonCover(element) {
    var id = getCartoonId(element.attr("href"));
    var title = element.attr("title");
    if (title.isEmpty()) {
        title = element.getElementsByTag("img").attr("alt");
    }
    var url = "https://www.cycanime.com" + element.attr("href");
    var coverUrl = element.getElementsByTag("img").attr("data-src");
    return new CartoonCoverImpl(id, "cyc", url, title, "intro", coverUrl);
}

/**
* 获取番剧id
* @param source 搜索源
* @param url 番剧url
* @return String 番剧id
*/
function getCartoonId(url) {
    var regex = new Regex("/(\\d+)");
    return regex.find(url, 0).groupValues[1];
}


function getCartoonPageDocById(id) {
    return Jsoup.connect("https://www.cycanime.com/bangumi/"+id+".html").userAgent(userAgent).get()
}

 /**
 * 获取番剧播放线路
 * @param id 番剧id
 * @param source 番剧源
 * @return List<PlayLine> 番剧播放线路
 * @see PlayLine
 */
function getPlayLineById(id) {
    var cartoonDoc = getCartoonPageDocById(id);
    var playLines = new ArrayList();
    var episodes = new ArrayList();
    var playLabel = cartoonDoc.selectXpath("/html/body/div[5]/div[2]/div[1]/div/a")
        .text().trim();
    var list = cartoonDoc.getElementsByClass("box border");
    for (var i = 0; i < list.size(); i++) {
        var episodeElement = list.get(i).getElementsByTag("a");
        var episodeId = Regex("/watch/(\\d+)/(\\d+)/(\\d+).html").find(episodeElement.attr("href"), 0).groupValues[3];
        var episodeOrder = i + 1;
        var episodeLabel = episodeElement.text().trim();
        episodes.add(
            new Episode(
                episodeId,
                episodeLabel,
                episodeOrder
            )
        );
    }
    var line = new PlayLine(
        "1" ,
        playLabel,
        episodes
    );
    playLines.add(line);

    return playLines;
}


/**
 * 获取番剧详情
 * @param id 番剧id
 * @param source 番剧源
 * @return CartoonImpl 番剧详情
 * @see CartoonImpl
 */
function getCartoonDetailById(id) {
    var videoDocument = getCartoonPageDocById(id)
    var title = videoDocument.getElementsByClass("slide-info-title hide").text()
    var coverUrl = videoDocument.getElementsByClass("detail-pic")[0].getElementsByTag("img").attr("data-src")
    var intro = videoDocument.getElementsByClass("text cor3")[0].text()
    var tagList =  videoDocument.getElementsByClass("slide-info hide").last().getElementsByTag("a")
    var tag = "";
    for (var i = 0; i < tagList.size(); i++) {
        tag += tagList.get(i).text().trim();
        if (i != tagList.size() - 1) {
            tag += ",";
        }
    }

    return new CartoonImpl(
        id,
        "cyc",
        "https://www.cycanime.com/bangumi"+id+".html",
        title,
        tag,
        coverUrl,
        intro,
       intro,
       0,
       false,
       0
    )
}
        """.trim()
    }
}