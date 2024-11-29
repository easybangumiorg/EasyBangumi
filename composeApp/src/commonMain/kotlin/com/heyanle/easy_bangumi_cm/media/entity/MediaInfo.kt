package com.heyanle.easy_bangumi_cm.media.entity

import kotlinx.datetime.Clock


/**
 * Created by HeYanLe on 2024/11/26 23:35.
 * https://github.com/heyanLE
 */
class MediaInfo(
    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    val source: String,          // 源 Key

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val intro: String,
    val url: String,

    // cartoon detailed
    val isDetailed: Boolean = false,
    val genre: String = "",          // 标签，为 "xx, xx"，标签 id
    val description: String = "",
    val updateStrategy: Int = UPDATE_STRATEGY_ALWAYS,
    val isUpdate: Boolean = false,
    val status: Int = STATUS_UNKNOWN,
    val lastUpdateTime: Long = 0L, // 最后更新的时间

    val isShowLine: Boolean = false,
    val sourceName: String = "",     // 源名称
    val reversal: Boolean = false, // 是否反转集数
    val sortByKey: String = "", // 排序名称

    val isPlayLineLoad: Boolean = false,
    val playLineString: String = "", // List<PlayLine> 的 json 数据，可能为 ""

    // star
    val tags: String = "", // 番剧分类 "1, 2, 3" 的格式
    val starTime: Long = 0, // 收藏时间，为 0 则为未收藏
    val upTime: Long = 0, // 置顶时间，为 0 则不置顶

    // history
    val lastHistoryTime: Long = 0, // 如果为 0 则代表没有历史记录
    val lastPlayLineEpisodeString: String = "", // 历史记录当前 play line 的 list<episode> 记录，已排序
    val lastLineId: String = "",
    val lastLinesIndex: Int = 0,
    val lastLineLabel: String = "",

    val lastEpisodeId: String = "",
    val lastEpisodeOrder: Int = 0,
    val lastEpisodeIndex: Int = 0,
    val lastEpisodeLabel: String = "",

    val lastTotalTile: Long = 0,
    val lastProcessTime: Long = 0,


    // other data
    val createTime: Long = Clock.System.now().toEpochMilliseconds(),
) {

    companion object {
        const val UPDATE_STRATEGY_ALWAYS = 0        // 总是更新
        const val UPDATE_STRATEGY_NEVER = 1         // 总是不更新
        const val UPDATE_STRATEGY_NO_AUTO = 2       // 非自动模式更新

        const val STATUS_UNKNOWN = 0    // 未知
        const val STATUS_GOING = 1      // 连载
        const val STATUS_COMPLETED = 2  // 完结
    }

    val genres: List<String> by lazy {
        if (genre.isEmpty()) {
            emptyList<String>()
        } else {
            genre.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }

    val tagList: List<String> by lazy {
        if (tags.isEmpty()) {
            emptyList()
        } else {
            tags.split(",").map { it.trim() }.filterNot { it.isBlank() }.distinct()
        }
    }



}