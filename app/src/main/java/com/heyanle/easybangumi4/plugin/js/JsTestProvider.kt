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
    return new Pair(null, res)

}
var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0";

/**
 * 获取推荐番剧
 * @return Elements 推荐番剧
 */
function getRecomElement() {
    return Jsoup.connect("https://www.cycanime.com").userAgent(userAgent).get().getElementsByClass("swiper-wrapper diy-center")[0].getElementsByClass("public-list-exp")
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
        """.trim()
    }
}