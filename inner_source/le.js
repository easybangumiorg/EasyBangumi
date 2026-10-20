// @key jisoft.ale
// @label 永乐视频
// @versionName 1.0
// @versionCode 1
// @libVersion 12
// @cover https://ylsp.tv/logo.png

// Inject
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var okhttpHelper = Inject_OkhttpHelper;
// Hook PreferenceComponent ========================================
function PreferenceComponent_getPreference() {
    var res = new ArrayList();
    var host = new SourcePreference.Edit("网页", "HostV2", "https://ylsp.tv");
    var timeout = new SourcePreference.Edit("超时时间", "Timeout", "10000");
    res.add(host);
    res.add(timeout);
    return res;
}

// Hook PageComment  ========================================

function PageComponent_getMainTabs() {
    var res = new ArrayList();
    res.add(new MainTab("电影", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("剧集", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("综艺", MainTab.MAIN_TAB_WITH_COVER));
    res.add(new MainTab("动漫", MainTab.MAIN_TAB_WITH_COVER));
    return res;
}

function PageComponent_getSubTabs(mainTab) {
    var res = new ArrayList();
    return res;
}

function PageComponent_getContent(mainTab, subTab, key) {
    var categoryIds = {
        "电影": 1,
        "剧集": 2,
        "动漫": 4,
        "综艺": 3
    };

    var categoryId = categoryIds[mainTab.label];
    if (!categoryId) {
        return new Pair(null, new ArrayList());
    }

    var url = "/vodshow/" + categoryId + "--hits------" + (key + 1) + "---/";

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
        // 从 /voddetail/117965/ 格式中提取ID
        var id = uu.replace("/voddetail/", "").replace("/", "");

        // 获取封面图片 - 优先使用data-original，回退到src
        var img = it.select("div.module-item-pic img").first();
        var coverUrl = "";
        if (img != null) {
            coverUrl = img.attr("data-original") || img.attr("src");
        }
        Log.d("getContent", "coverUrl: " + coverUrl);

        // 获取标题 - 优先使用title属性，回退到文本内容
        var title = it.attr("title") || it.select("div.module-poster-item-title").text();

        // 获取介绍信息
        var intro = it.select("div.module-item-note").text();

        list.add(
            makeCartoonCover({
               id: id,
               source: source.key,
               url: uu,
               title: title,
               intro: intro,
               cover: SourceUtils.urlParser(getRootUrl(), coverUrl),
           })
        );
    }
    return list;
}

// Hook DetailedComponent ========================================

 function DetailedComponent_getDetailed(summary) {
    var u = SourceUtils.urlParser(getRootUrl(), "/voddetail/"+summary.id+"/");
    var doc = getDoc(u);
    JSLogUtils.d("doc", doc);
    var cartoon = detailed(doc, summary);
    var playLine = playline(doc, summary);
    return new Pair(cartoon, playLine);
 }

 function detailed(doc, summary) {
    // 获取标题 - 从h1元素中提取
    var titleEle = doc.select("div.module-info-heading h1").first();
    var title = "";
    if (titleEle != null) {
        title = titleEle.text().trim();
    }

    // 获取封面图片 - 优先使用data-original，回退到src
    var coverEle = doc.select("div.module-item-pic img").first();
    var cover = "";
    if (coverEle != null) {
        cover = coverEle.attr("data-original") || coverEle.attr("src");
        // 如果是相对路径，转换为绝对路径
        if (cover && !cover.startsWith("http")) {
            cover = SourceUtils.urlParser(getRootUrl(), cover);
        }
    }

    // 获取描述信息 - 从module-info-introduction-content中提取
    var descEle = doc.select("div.module-info-introduction-content p").first();
    var desc = "";
    if (descEle != null) {
        desc = descEle.text().trim();
    }

    // 获取类型信息 - 从module-info-tag中提取
    var genreTags = doc.select("div.module-info-tag-link a");
    var genre = "";
    if (genreTags.size() > 0) {
        var genres = [];
        for (var i = 0; i < genreTags.size(); i++) {
            var tag = genreTags.get(i);
            var tagText = tag.text().trim();
            if (tagText && tagText !== "2025") { // 排除年份标签
                genres.push(tagText);
            }
        }
        genre = genres.join(", ");
    }

    return makeCartoon({
        id: summary.id,
        url: SourceUtils.urlParser(getRootUrl(), "/voddetail/" + summary.id + "/"),
        source: summary.source,
        title: title,
        cover: SourceUtils.urlParser(getRootUrl(), cover),
        intro: "",
        description: desc,
        genre: genre || null,
        status: Cartoon.STATUS_UNKNOWN,
        updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS,
    });
 }
 function playline(doc, summary) {
    // 获取播放线路标签 - 遵循KISS原则，使用更直接的选择器
    var tabElements = doc.select("div.module-tab-items-box div.module-tab-item");
    // 获取对应的剧集列表 - 每个线路对应一个module-list
    var episodeLists = doc.select("div.module-list.sort-list.tab-list.his-tab-list");

    var playLines = new ArrayList();

    // 遍历播放线路和对应的剧集列表 - 遵循DRY原则，避免重复代码
    for (var i = 0; i < tabElements.size() && i < episodeLists.size(); i++) {
        var tabElement = tabElements.get(i);
        var episodeList = episodeLists.get(i);

        // 获取线路名称 - 优先使用data-dropdown-value，回退到span文本
        var lineName = tabElement.attr("data-dropdown-value") ||
                      tabElement.select("span").text().trim();

        // 解析剧集列表 - 简化逻辑，直接选择链接元素
        var episodes = new ArrayList();
        var episodeLinks = episodeList.select("a.module-play-list-link");

        for (var j = 0; j < episodeLinks.size(); j++) {
            var link = episodeLinks.get(j);
            var episodeTitle = link.select("span").text().trim();

            // 简化剧集ID生成 - 遵循KISS原则，直接使用序号
            var episodeId = (j + 1).toString();

            episodes.add(
                new Episode(
                    id = episodeId,
                    label = episodeTitle,
                    order = j
                )
            );
        }

        // 创建播放线路 - 使用线路索引作为ID
        playLines.add(
            new PlayLine(
                id = (i + 1).toString(),
                label = lineName,
                episode = episodes
            )
        );
    }

    return playLines;
}

 // Hook SearchComponent ========================================
 function SearchComponent_search(page, keyword) {
    var url = SourceUtils.urlParser(
        getRootUrl(),
        "/vodsearch/" + URLEncoder.encode(keyword, "utf-8") + "----------" + (page + 1) + "---/"
    );
    var doc = getDoc(url);
    var res = new ArrayList();
    var elements = doc.select("div.module-card-item.module-item");

    for (var i = 0; i < elements.size(); i++) {
        var it = elements.get(i);

        // 获取详情页链接 - 遵循KISS原则，直接提取href
        var uu = it.select("a.module-card-item-poster").attr("href");
        if (!uu) continue; // 跳过无效项

        // 提取ID - 从 /voddetail/92169/ 格式中提取
        var id = uu.replace("/voddetail/", "").replace("/", "");

        // 获取封面图片 - 优先使用data-original，回退到src
        var imgEle = it.select("div.module-item-pic img").first();
        var coverUrl = "";
        if (imgEle != null) {
            coverUrl = imgEle.attr("data-original") || imgEle.attr("src");
        }

        // 获取标题 - 从strong标签中提取
        var titleEle = it.select("div.module-card-item-title strong").first();
        var title = "";
        if (titleEle != null) {
            title = titleEle.text().trim();
        }

        // 获取介绍信息 - 合并多个module-info-item-content的内容
        var introElements = it.select("div.module-info-item-content");
        var introParts = [];
        for (var j = 0; j < introElements.size(); j++) {
            var content = introElements.get(j).text().trim();
            if (content) {
                introParts.push(content);
            }
        }
        var intro = introParts.join(" / ");

        // 创建动漫封面对象 - 遵循DRY原则，统一使用makeCartoonCover
        var cartoonCover = makeCartoonCover({
            id: id,
            title: title,
            url: SourceUtils.urlParser(getRootUrl(), uu),
            intro: intro,
            cover: SourceUtils.urlParser(getRootUrl(), coverUrl),
            source: source.key,
        });
        res.add(cartoonCover);
    }

    // 遵循YAGNI原则，简化返回逻辑
    return res.size() == 0 ?
        new Pair(null, new ArrayList()) :
        new Pair(page + 1, res);
 }

 // Hook PlayComponent ========================================
function PlayComponent_getPlayInfo(summary, playLine, episode) {
    var url = JSSourceUtils.urlParser(getRootUrl(), "/play/"+summary.id+"-"+playLine.id+"-"+episode.id+"/");

   var doc = getDoc(url);
    var res = "";
    // 精准匹配 var player_aaaa 对象中的 url 字段 - 遵循KISS原则，使用更精确的正则表达式
    // 使用非贪婪匹配和多行模式来处理复杂的JSON结构
    var urlPattern = /var player_aaaa\s*=\s*\{[\s\S]*?"url"\s*:\s*"([^"]+)"[\s\S]*?\}/;
    var urlMatch = doc.toString().match(urlPattern);

    if (urlMatch != null && urlMatch.length > 1) {
        res = urlMatch[1];
        // 去掉URL中的转义斜杠 - 处理JSON中的转义字符
        res = res.replace(/\\\//g, "/");
    }

    if(res.length == 0) {
        Log.e("PlayComponent", "视频URL解析失败 url为："+res);
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
    return preferenceHelper.get("HostV2", "https://ylsp.tv");
}
