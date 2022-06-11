package com.heyanle.easybangumi.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 番剧详情
 * Created by HeYanLe on 2021/9/8 22:11.
 * https://github.com/heyanLE
 */
@Entity(tableName = "bangumi_detail")
data class BangumiDetail(

    // 唯一标识，格式 "[source]-[id]"
    @PrimaryKey var id: String,

    // 源名称
    var source: String,

    // 名称
    var name: String = "",

    // 封面
    var cover: String = "",

    // 简介
    var intro: String = "",

    // 详细网址
    val detailUrl: String,

    // 描述
    var description : String = "",

    // 是否追番
    var star: Boolean = false,

    // 上次观看线路
    var lastLine: Int = 0,

    // 上次观看进度 时间
    var lastProcessTime: Long = 0,

    // 上次观看集数
    var lastEpisodes: Int = 0,

    var lastEpisodeTitle: String = "",

    var lastVisiTime: Long = 0L,

){
    fun getBangumi():Bangumi{
        return Bangumi(id, source, detailUrl, name, cover, intro)
    }
}