// @key ayala.jellyfin
// @label Easy Jellyfin Beta
// @versionName 1.7
// @versionCode 8
// @libVersion 11
// @cover https://jellyfin.org/images/favicon.ico

// 公共代码开始 ========================================
var networkHelper = Inject_NetworkHelper;
var preferenceHelper = Inject_PreferenceHelper;
var okHttpHelper = Inject_OkhttpHelper;
var webViewHelperV2 = Inject_WebViewHelperV2;
var stringHelper = Inject_StringHelper;

function BetterPlugin(dev_config) {
    var preferences = new ArrayList();
    var pagemap = new HashMap();
    var onBeforePreference_hooks = [];
    var onBeforePreference_ready = false;
    var onBeforeMainTab_hooks = [];
    var onBeforeMainTab_ready = false;

    preferences.add(new SourcePreference.Switch(
        "调试模式（非调试状态请勿打开）",
        "ayala.sbr.debug",
        dev_config.debug_mode
    ));

    preferences.add(new SourcePreference.Edit(
        "调试服务器地址",
        "ayala.sbr.devServer",
        dev_config.debug_server
    ))

    if (dev_config.production_mode)
        preferenceHelper.put("ayala.sbr.debug", 'false');

    var log = {
        raw(object) {
            if (preferenceHelper.get("ayala.sbr.debug", dev_config.debug_mode) == 'true') {
                var debug_server = preferenceHelper.get("ayala.sbr.devServer", dev_config.debug_server);
                var client = okHttpHelper.client
                var requestBody = RequestBody.create(
                    MediaType.get('application/json; charset=utf-8'),
                    JSON.stringify(object));
                var request = new Request.Builder()
                    .url(debug_server + "/log")
                    .post(requestBody)
                    .build();
                var response = client.newCall(request).execute();
                var responseBody = response.body().string();
                return responseBody;
            }
        },
        i(label, msg) {
            JSLogUtils.i(label, JSON.stringify(msg));
            return this.raw({
                level: "info",
                label: label,
                msg: msg,
            });
        },
        w(label, msg) {
            JSLogUtils.w(label, JSON.stringify(msg));
            return this.raw({
                level: "warn",
                label: label,
                msg: msg,
            });
        },
        e(label, msg) {
            JSLogUtils.e(label, JSON.stringify(msg));
            return this.raw({
                level: "error",
                label: label,
                msg: msg,
            });
        },
        stackTrace(label, error) {
            return this.raw({
                level: "error",
                label: label,
                msg: error.message + '\n' + error.stack,
            });
        }
    }

    function checkSubTab(mainTabLabel) {
        var page = pagemap.get(mainTabLabel);
        if (page.subtab == null) {
            log.i("plugin._checkSubTab", "计算子页面：" + mainTabLabel);
            var tab = {
                type: 'none',
                content: new HashMap(),
                only(callback) {
                    this.type = 'only';
                    this.content = callback;
                },
                subpage(name, index, is_active, callback) {
                    if (this.type == 'only') {
                        log.w("plugin._checkSubTab", "页面已被设置为only，将跳过设置subpage：" + mainTabLabel);
                        return
                    };
                    this.type = 'pages';
                    this.content.put(name, {
                        name: name,
                        callback: callback,
                        index: index,
                        is_active: is_active
                    });
                }
            }
            page.callback(tab);
            page.subtab = tab;
            if (page.subtab.type == 'none')
                log.e("plugin._checkSubTab", "页面未设置tab，这将导致在后续的过程中报错：" + mainTabLabel);
            if (page.subtab.type == 'pages')
                page.page = new MainTab(mainTabLabel, MainTab.MAIN_TAB_GROUP);
            if (page.subtab.type == 'only')
                page.page = new MainTab(mainTabLabel, MainTab.MAIN_TAB_WITH_COVER);
            pagemap.put(mainTabLabel, page);
        }
        return page
    }

    return {
        log: log,
        onBeforePreference(hook) {
            onBeforePreference_hooks.push(hook);
        },
        onBeforeMainTab(hook) {
            onBeforeMainTab_hooks.push(hook);
        },
        page(name, callback) {
            pagemap.put(name, {
                page: null,
                callback: callback,
                subtab: null,
                index: pagemap.keySet().size()
            });
        },
        _onBeforePreference() {
            if (onBeforePreference_ready) return;
            onBeforePreference_hooks.forEach(hook => hook(preferences));
            onBeforePreference_ready = true;
        },
        _getPreference: () => preferences,
        _onBeforeMainTab() {
            if (onBeforeMainTab_ready) return;
            onBeforeMainTab_hooks.forEach(hook => hook());
            onBeforeMainTab_ready = true;
        },
        _getMainTabs() {
            log.i("plugin._getMainTabs", "获取所有页面");
            var tabs = new ArrayList();
            pagemap.keySet().forEach(tab => {
                tabs.add(checkSubTab(tab));
            })
            tabs.sort((a, b) => a.index - b.index);
            var main_tabs = new ArrayList();
            tabs = tabs.forEach(tab => main_tabs.add(tab.page));
            return main_tabs;
        },
        _getSubTabs(mainTab) {
            var page = checkSubTab(mainTab.label);
            switch (page.subtab.type) {
                case 'pages':
                    var tabs = new ArrayList();
                    page.subtab.content.keySet().forEach(key => {
                        var value = page.subtab.content.get(key);
                        tabs.add(new SubTab(value.name, value.is_active, value.index));
                    })
                    tabs.sort((a, b) => a.ext - b.ext);
                    return tabs;
                default:
                    return new ArrayList();
            }
        },
        _getContent(mainTab, subTab, page) {
            var page = checkSubTab(mainTab.label);
            switch (page.subtab.type) {
                case 'only':
                    log.i("plugin._getContent", "获取页面内容：" + mainTab.label);
                    return page.subtab.content(page);
                case 'pages':
                    log.i("plugin._getContent", "获取页面内容：" + mainTab.label + " - " + subTab.label);
                    var subTab = page.subtab.content.get(subTab.label);
                    return subTab.callback(page);
            }
            return new Pair(null, new ArrayList());
        },
    }
}

var plugin = BetterPlugin({
    production_mode: true,
    debug_mode: false,
    debug_server: "http://example.com",
});

function PreferenceComponent_getPreference() {
    plugin._onBeforePreference();
    return plugin._getPreference();
}

function PageComponent_getMainTabs() {
    plugin._onBeforeMainTab();
    return plugin._getMainTabs();
}

function PageComponent_getSubTabs(mainTab) {
    return plugin._getSubTabs(mainTab);
}

function PageComponent_getContent(mainTab, subTab, page) {
    return plugin._getContent(mainTab, subTab, page);
}

// 项目代码开始 ========================================

// issue: 暂时还不知道JS的完全等于===运算在Rhino中是否有用

var default_preference = { // 默认设置
    url: "",
    apikey: "请输入你的API密钥",
    userid: "请输入你的用户ID",
    maxbitrate: "217482430",
    allow_transcode: true,
}

plugin.onBeforePreference(preference => {
    preference.add(new SourcePreference.Edit(
        "Jellyfin地址",
        "ayala.sbr.url",
        default_preference.url
    ));
    preference.add(new SourcePreference.Edit(
        "API密钥",
        "ayala.sbr.apikey",
        default_preference.apikey
    ));
    preference.add(new SourcePreference.Edit(
        "用户ID",
        "ayala.sbr.userid",
        default_preference.userid
    ));
    preference.add(new SourcePreference.Switch(
        "是否允许转码播放",
        "ayala.sbr.transcode",
        default_preference.allow_transcode
    ));
    preference.add(new SourcePreference.Edit(
        "最大比特率",
        "ayala.sbr.maxbitrate",
        default_preference.maxbitrate
    ));
});


function trancode_profile() {
    var profile = {
        "DeviceProfile": {
            "MaxStreamingBitrate": 120000000,
            "MaxStaticBitrate": 100000000,
            "MusicStreamingTranscodingBitrate": 384000,
            "DirectPlayProfiles": [
                { "Container": "mp4,m4v", "Type": "Video", "VideoCodec": "h264,hevc,vp9,av1", "AudioCodec": "aac,mp3,mp2,opus,flac,vorbis" },
                { "Container": "hls", "Type": "Video", "VideoCodec": "av1,hevc,h264", "AudioCodec": "aac,mp2,opus,flac" },
                { "Container": "webm", "Type": "Video", "VideoCodec": "vp8,vp9,av1", "AudioCodec": "vorbis,opus" },
                { "Container": "mov", "Type": "Video", "VideoCodec": "h264", "AudioCodec": "aac,mp3,mp2,opus,flac,vorbis" },
                { "Container": "opus", "Type": "Audio" },
                { "Container": "webm", "AudioCodec": "opus", "Type": "Audio" },
                { "Container": "mp3", "Type": "Audio" },
                { "Container": "aac", "Type": "Audio" },
                { "Container": "m4a", "AudioCodec": "aac", "Type": "Audio" },
                { "Container": "m4b", "AudioCodec": "aac", "Type": "Audio" },
                { "Container": "flac", "Type": "Audio" },
                { "Container": "webma", "Type": "Audio" },
                { "Container": "webm", "AudioCodec": "webma", "Type": "Audio" },
                { "Container": "wav", "Type": "Audio" },
                { "Container": "ogg", "Type": "Audio" },
                { "Container": "hls", "Type": "Video", "VideoCodec": "h264", "AudioCodec": "aac,mp3,mp2" }
            ],
            "TranscodingProfiles": [
                { "Container": "mp4", "Type": "Video", "AudioCodec": "aac,mp2,opus,flac", "VideoCodec": "av1,hevc,h264", "Context": "Streaming", "Protocol": "hls", "MaxAudioChannels": "2", "MinSegments": "1", "BreakOnNonKeyFrames": true },
                { "Container": "mp4", "Type": "Audio", "AudioCodec": "aac", "Context": "Streaming", "Protocol": "hls", "MaxAudioChannels": "2", "MinSegments": "1", "BreakOnNonKeyFrames": true },
                { "Container": "aac", "Type": "Audio", "AudioCodec": "aac", "Context": "Streaming", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "mp3", "Type": "Audio", "AudioCodec": "mp3", "Context": "Streaming", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "opus", "Type": "Audio", "AudioCodec": "opus", "Context": "Streaming", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "wav", "Type": "Audio", "AudioCodec": "wav", "Context": "Streaming", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "opus", "Type": "Audio", "AudioCodec": "opus", "Context": "Static", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "mp3", "Type": "Audio", "AudioCodec": "mp3", "Context": "Static", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "aac", "Type": "Audio", "AudioCodec": "aac", "Context": "Static", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "wav", "Type": "Audio", "AudioCodec": "wav", "Context": "Static", "Protocol": "http", "MaxAudioChannels": "2" },
                { "Container": "ts", "Type": "Video", "AudioCodec": "aac,mp3,mp2", "VideoCodec": "h264", "Context": "Streaming", "Protocol": "hls", "MaxAudioChannels": "2", "MinSegments": "1", "BreakOnNonKeyFrames": true }
            ],
            "ContainerProfiles": [],
            "CodecProfiles": [
                { "Type": "VideoAudio", "Codec": "aac", "Conditions": [{ "Condition": "Equals", "Property": "IsSecondaryAudio", "Value": "false", "IsRequired": false }] },
                { "Type": "VideoAudio", "Conditions": [{ "Condition": "Equals", "Property": "IsSecondaryAudio", "Value": "false", "IsRequired": false }] },
                { "Type": "Video", "Codec": "hevc", "Conditions": [{ "Condition": "NotEquals", "Property": "IsAnamorphic", "Value": "true", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoProfile", "Value": "main|main 10", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoRangeType", "Value": "SDR|HDR10|HLG", "IsRequired": false }, { "Condition": "LessThanEqual", "Property": "VideoLevel", "Value": "183", "IsRequired": false }, { "Condition": "NotEquals", "Property": "IsInterlaced", "Value": "true", "IsRequired": false }] },
                { "Type": "Video", "Codec": "h264", "Conditions": [{ "Condition": "NotEquals", "Property": "IsAnamorphic", "Value": "true", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoProfile", "Value": "high|main|baseline|constrained baseline|high 10", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoRangeType", "Value": "SDR", "IsRequired": false }, { "Condition": "LessThanEqual", "Property": "VideoLevel", "Value": "52", "IsRequired": false }, { "Condition": "NotEquals", "Property": "IsInterlaced", "Value": "true", "IsRequired": false }] },
                { "Type": "Video", "Codec": "vp9", "Conditions": [{ "Condition": "EqualsAny", "Property": "VideoRangeType", "Value": "SDR|HDR10|HLG", "IsRequired": false }] },
                { "Type": "Video", "Codec": "av1", "Conditions": [{ "Condition": "NotEquals", "Property": "IsAnamorphic", "Value": "true", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoProfile", "Value": "main", "IsRequired": false }, { "Condition": "EqualsAny", "Property": "VideoRangeType", "Value": "SDR|HDR10|HLG", "IsRequired": false }, { "Condition": "LessThanEqual", "Property": "VideoLevel", "Value": "19", "IsRequired": false }] }
            ],
            "SubtitleProfiles": [
                { "Format": "vtt", "Method": "External" },
                { "Format": "ass", "Method": "External" },
                { "Format": "ssa", "Method": "External" }
            ],
            "ResponseProfiles": [{ "Type": "Video", "Container": "m4v", "MimeType": "video/mp4" }]
        }
    }

    return profile
}

var network = {
    parse_url: (path) => JSSourceUtils.urlParser(preferenceHelper.get("ayala.sbr.url", default_preference.url), path),
    api_key: () => preferenceHelper.get("ayala.sbr.apikey", default_preference.apikey),
    auth() {
        return `MediaBrowser Client="EasyBangumi", Version="1.0.2", Token="${this.api_key()}"`
    },
    client: okHttpHelper.client,
    post(url, body) {
        plugin.log.i("network.post", this.parse_url(url))
        var requestBody = RequestBody.create(
            MediaType.get('application/json; charset=utf-8'),
            JSON.stringify(body));
        var request = new Request.Builder()
            .url(this.parse_url(url))
            .addHeader("Authorization", this.auth())
            .post(requestBody)
            .build();
        var response = this.client.newCall(request).execute();
        return JSON.parse(response.body().string());
    },
    get(url) {
        plugin.log.i("network.get", this.parse_url(url))
        var request = new Request.Builder()
            .url(this.parse_url(url))
            .addHeader("Authorization", this.auth())
            .get()
            .build();
        var response = this.client.newCall(request).execute();
        return JSON.parse(response.body().string());
    }
}

plugin.onBeforeMainTab(() => {
    try {
        var librarys = network.get("/Library/VirtualFolders");
        var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
        librarys.forEach(library => {
            plugin.page(library.Name, tab => {
                var type = library.CollectionType;
                tab.only(page => {
                    var items = network.get(`/Users/${userId}/Items?SortBy=SortName&SortOrder=Ascending&ParentId=${library.ItemId}`)
                    var list = new ArrayList();
                    items.Items.forEach(item => {
                        var cover = `/Items/${item.Id}/Images/Primary?fillHeight=316&fillWidth=213&quality=96&tag=${item.ImageTags.Primary}`
                        list.add(makeCartoonCover({
                            id: item.Id,
                            url: item.Id,
                            title: item.Name,
                            cover: network.parse_url(cover),
                            intro: ""
                        }))
                    })
                    return new Pair(null, list);
                })
            })
        })
    } catch (error) {
        plugin.log.stackTrace("Jellyfin", error)
        if (preferenceHelper.get("ayala.sbr.url", default_preference.url) != default_preference.url) {
            stringHelper.toast("Jellyfin连接失败")
        }
    }
})

function DetailedComponent_getDetailed(summary) {
    try {
        var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
        var detail = network.get(`/Users/${userId}/Items/${summary.id}`);
        var info = makeCartoon({
            id: detail.Id,
            url: detail.Id,
            title: detail.Name,
            cover: network.parse_url(`/Items/${detail.Id}/Images/Primary?fillHeight=316&fillWidth=213&quality=96&tag=${detail.ImageTags.Primary}`),
            intro: detail.Type,
            description: detail.Overview,
            genreList: new ArrayList(detail.Tags),
            updateStrategy: Cartoon.UPDATE_STRATEGY_ALWAYS,
            isUpdate: false,
            status: Cartoon.STATUS_UNKNOWN,
        })
        var playLines = new ArrayList();
        switch (detail.Type) {
            case "Movie":
                playLines.addAll(getMoviePlayLines(detail));
                break;
            case "Series":
                playLines.addAll(getSeriesPlayLines(detail));
                break;
            default:
                stringHelper.toast("暂不支持的类型：" + detail.Type)
        }
        return new Pair(info, playLines);
    } catch (error) {
        plugin.log.stackTrace("Jellyfin", error);
        throw error;
    }
}

function getMoviePlayLines(info) {
    var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
    var play_lines = new PlayLine(info.Id, info.Name, new ArrayList());
    info.MediaSources.forEach((source, index) => {
        play_lines.episode.add(new Episode(source.Id, source.Name, index));
    })
    return new ArrayList([play_lines]);
}

function getSeriesPlayLines(info) {
    var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
    var play_lines_info = network.get(`/Shows/${info.Id}/Seasons?userId=${userId}`)
    var play_lines = new ArrayList();

    play_lines_info.Items.forEach(season => {
        var play_line = new PlayLine(season.Id, season.Name, new ArrayList());
        var episodes = network.get(`/Shows/${info.Id}/Episodes?seasonId=${season.Id}&userId=${userId}`)

        episodes.Items.forEach((episode, index) => {
            play_line.episode.add(new Episode(episode.Id, `${index + 1} ${episode.Name}`, index));
        })

        play_lines.add(play_line);
    })

    return play_lines;
}

function PlayComponent_getPlayInfo(summary, playLine, episode) {
    try {
        var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
        var apiId = preferenceHelper.get("ayala.sbr.apikey", default_preference.apikey);
        var maxbitrate = preferenceHelper.get("ayala.sbr.maxbitrate", default_preference.maxbitrate);
        var allow_transcode = preferenceHelper.get("ayala.sbr.transcode", default_preference.allow_transcode);
        var play_info = network.post(
            `/Items/${episode.id}/PlaybackInfo?UserId=${userId}&StartTimeTicks=0&IsPlayback=false&AutoOpenLiveStream=true&MaxStreamingBitrate=${maxbitrate}`,
            trancode_profile()
        );

        var play_infos = play_info.MediaSources[0]
        var play_type = play_infos.TranscodingSubProtocol

        if (allow_transcode != 'true')
            play_type = "http" // 降级为http播放

        switch (play_type) {
            case "hls":
                var url = network.parse_url(play_infos.TranscodingUrl)
                plugin.log.i(`Jellyfin Play(${play_type})`, url)
                return new PlayerInfo(PlayerInfo.DECODE_TYPE_HLS, url);
            case "http":
                var url = network.parse_url(`/Videos/${play_infos.Id}/stream.mp4?Static=true&mediaSourceId=${play_infos.Id}&api_key=${apiId}&Tag=${play_infos.ETag}`)
                plugin.log.i(`Jellyfin Play(${play_type})`, url)
                return new PlayerInfo(PlayerInfo.DECODE_TYPE_OTHER, url)
            default:
                return new PlayerInfo(PlayerInfo.DECODE_TYPE_OTHER, network.parse_url(""));
        }
    } catch (error) {
        plugin.log.stackTrace("Jellyfin", error);
        throw error;
    }
}

function SearchComponent_search(page, keyword) {
    try {
        var userId = preferenceHelper.get("ayala.sbr.userid", default_preference.userid);
        var search = network.get(`/Users/${userId}/Items?searchTerm=${URLEncoder.encode(keyword, "utf-8")}&Limit=100&Recursive=true&EnableTotalRecordCount=false&ImageTypeLimit=1&IncludePeople=false&IncludeMedia=true&IncludeGenres=false&IncludeStudios=false&IncludeArtists=false&IncludeItemTypes=Series`);

        var list = new ArrayList();
        search.Items.forEach(item => {
            var cover = `/Items/${item.Id}/Images/Primary?fillHeight=316&fillWidth=213&quality=96&tag=${item.ImageTags.Primary}`
            list.add(makeCartoonCover({
                id: item.Id,
                url: item.Id,
                title: item.Name,
                cover: network.parse_url(cover),
                intro: `社区评分：${item.CommunityRating}`
            }))
        })
        return new Pair(null, list);
    } catch (error) {
        plugin.log.stackTrace("Jellyfin", error);
        throw error;
    }
}
