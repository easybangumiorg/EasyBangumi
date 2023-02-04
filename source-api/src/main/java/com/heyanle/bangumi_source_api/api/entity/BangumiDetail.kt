package com.heyanle.bangumi_source_api.api.entity


/**
 * 番剧详情
 * Created by HeYanLe on 2021/9/8 22:11.
 * https://github.com/heyanLE
 */
data class BangumiDetail(

    // 唯一标识，格式 "[source]-[id]"
    var id: String,

    // 源名称
    val source: String,

    // 名称
    val name: String = "",

    // 封面
    val cover: String = "",

    // 简介
    val intro: String = "",

    // 详细网址
    val detailUrl: String,

    // 描述
    val description: String = "",

    // 是否追番
    var star: Boolean = false,

    // 上次观看线路
    var lastLine: Int = 0,

    // 上次观看进度 时间
    var lastProcessTime: Long = 0,

    // 上次观看集数
    var lastEpisodes: Int = 0,

    var lastEpisodeTitle: String = "",

    var lastVisitedTime: Long = 0L,

    var createTime: Long = 0L,


    ) {
    fun getBangumi(): Bangumi {
        return Bangumi(id, source, detailUrl, name, cover, intro)
    }

    fun getSummary(): BangumiSummary {
        return BangumiSummary(source, detailUrl)
    }

}