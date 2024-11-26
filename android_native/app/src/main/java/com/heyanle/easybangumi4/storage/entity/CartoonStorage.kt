package com.heyanle.easybangumi4.storage.entity

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.source_api.entity.Cartoon

/**
 * 备份用，会转成 Json
 * 方便后续拓展
 * Created by heyanle on 2024/7/26.
 * https://github.com/heyanLE
 */
data class CartoonStorage (
    // finder
    val id: String,              // 标识，由源自己支持，用于区分番剧
    val source: String,

    // cartoonCover
    val name: String,
    val coverUrl: String,
    val intro: String,
    val url: String,

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
) {

    companion object {

        fun fromCartoonInfo(
            cartoonInfo: CartoonInfo,
            star: Boolean = true,
            history: Boolean = true,
        ): CartoonStorage {
            return CartoonStorage(
                id = cartoonInfo.id,
                source = cartoonInfo.source,
                name = cartoonInfo.name,
                coverUrl = cartoonInfo.coverUrl,
                intro = cartoonInfo.intro,
                url = cartoonInfo.url,
                isDetailed = cartoonInfo.isDetailed,
                genre = cartoonInfo.genre,
                description = cartoonInfo.description,
                updateStrategy = cartoonInfo.updateStrategy,
                isUpdate = cartoonInfo.isUpdate,
                status = cartoonInfo.status,
                lastUpdateTime = cartoonInfo.lastUpdateTime,
                isShowLine = cartoonInfo.isShowLine,
                sourceName = cartoonInfo.sourceName,
                reversal = cartoonInfo.reversal,
                sortByKey = cartoonInfo.sortByKey,
                isPlayLineLoad = cartoonInfo.isPlayLineLoad,
                playLineString = cartoonInfo.playLineString,
                tags =if (star) cartoonInfo.tags else "",
                starTime = if (star) cartoonInfo.starTime else 0L,
                upTime = if (star)  cartoonInfo.upTime else 0L,
                lastHistoryTime = if (history) cartoonInfo.lastHistoryTime else 0L,
                lastPlayLineEpisodeString = cartoonInfo.lastPlayLineEpisodeString,
                lastLineId = cartoonInfo.lastLineId,
                lastLinesIndex = cartoonInfo.lastLinesIndex,
                lastLineLabel = cartoonInfo.lastLineLabel,
                lastEpisodeId = cartoonInfo.lastEpisodeId,
                lastEpisodeOrder = cartoonInfo.lastEpisodeOrder,
                lastEpisodeIndex = cartoonInfo.lastEpisodeIndex,
                lastEpisodeLabel = cartoonInfo.lastEpisodeLabel,
                lastTotalTile = cartoonInfo.lastTotalTile,
                lastProcessTime = cartoonInfo.lastProcessTime,
                createTime = cartoonInfo.createTime
            )
        }
    }

    fun toCartoonInfo(): CartoonInfo {
        return CartoonInfo(
            id = id,
            source = source,
            name = name,
            coverUrl = coverUrl,
            intro = intro,
            url = url,
            isDetailed = isDetailed,
            genre = genre,
            description = description,
            updateStrategy = updateStrategy,
            isUpdate = isUpdate,
            status = status,
            lastUpdateTime = lastUpdateTime,
            isShowLine = isShowLine,
            sourceName = sourceName,
            reversal = reversal,
            sortByKey = sortByKey,
            isPlayLineLoad = isPlayLineLoad,
            playLineString = playLineString,
            tags = tags,
            starTime = starTime,
            upTime = upTime,
            lastHistoryTime = lastHistoryTime,
            lastPlayLineEpisodeString = lastPlayLineEpisodeString,
            lastLineId = lastLineId,
            lastLinesIndex = lastLinesIndex,
            lastLineLabel = lastLineLabel,
            lastEpisodeId = lastEpisodeId,
            lastEpisodeOrder = lastEpisodeOrder,
            lastEpisodeIndex = lastEpisodeIndex,
            lastEpisodeLabel = lastEpisodeLabel,
            lastTotalTile = lastTotalTile,
            lastProcessTime = lastProcessTime,
            createTime = createTime
        )
    }

}