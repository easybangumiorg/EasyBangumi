package com.heyanle.lib_anim.entity

/**
 * Created by HeYanLe on 2023/1/11 15:38.
 * https://github.com/heyanLE
 */
// 番剧摘要
data class BangumiSummary(
    // 源名称
    val source: String,

    // 详细网址
    val detailUrl: String,
){

    override fun toString(): String {
        return "BangumiSummary(source='$source', detailUrl='$detailUrl')"
    }
}