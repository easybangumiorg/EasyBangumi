package com.heyanle.bangumi_source_api.api.entity

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
) {


    override fun toString(): String {
        return "BangumiSummary(source='$source', detailUrl='$detailUrl')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BangumiSummary

        if (source != other.source) return false
        if (detailUrl != other.detailUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + detailUrl.hashCode()
        return result
    }
}