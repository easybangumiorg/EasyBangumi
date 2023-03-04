package com.heyanle.bangumi_source_api.api.entity

import java.io.Serializable

/**
 * Created by HeYanLe on 2023/2/18 21:30.
 * https://github.com/heyanLE
 */
class CartoonSummary(
    var id: String,              // 标识，由源自己支持，用于区分番剧
    var source: String,
    var url: String,
) : Serializable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonSummary

        if (id != other.id) return false
        if (source != other.source) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }


}