package com.heyanle.easybangumi4.cartoon.old.entity

import androidx.room.Entity
import com.heyanle.easybangumi4.source_api.entity.Cartoon

/**
 * Created by heyanle on 2024/1/28.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonInfo",primaryKeys = ["id", "source", "url"])
data class CartoonInfoV1(

    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    val source: String,
    val url: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val intro: String,

    // cartoon detailed
    val isDetailed: Boolean = false,
    val genre: String = "",          // 标签，为 "xx, xx"，标签 id
    val description: String = "",
    val updateStrategy: Int = Cartoon.UPDATE_STRATEGY_ALWAYS,
    val isUpdate: Boolean = false,
    val status: Int = Cartoon.STATUS_UNKNOWN,
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
    val createTime: Long = System.currentTimeMillis(),
)