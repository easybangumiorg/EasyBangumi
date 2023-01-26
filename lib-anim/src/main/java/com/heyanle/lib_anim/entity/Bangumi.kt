package com.heyanle.lib_anim.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2021/9/8 22:09.
 * https://github.com/heyanLE
 */
@Entity
data class Bangumi(

    // 唯一标识，格式 "[source]-[id]"
    @PrimaryKey var id: String,

    // 源名称
    val source: String,

    // 详细网址
    val detailUrl: String,

    // 名称
    val name: String = "",

    // 封面
    val cover: String = "",

    // 简介
    val intro: String = "",

    // 最后访问时间 （历史记录）
    var visitTime: Long = 0L,
) {
    fun toSummary(): BangumiSummary {
        return BangumiSummary(source, detailUrl)
    }
}