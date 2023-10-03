package com.heyanle.easybangumi4.download.entity

import com.heyanle.easybangumi4.utils.getMatchReg

/**
 * Created by HeYanLe on 2023/9/17 15:41.
 * https://github.com/heyanLE
 */
data class LocalCartoon (
    // cartoon 外键
    val cartoonId: String,
    val cartoonUrl: String,
    val cartoonSource: String,

    // 展示数据
    val sourceLabel: String,
    val cartoonTitle: String,
    val cartoonCover: String,
    val cartoonDescription: String,
    val cartoonGenre: String,

    var playLines: ArrayList<LocalPlayLine> = arrayListOf(),
){

    fun match(query: String): Boolean{
        var matched = false
        for (match in query.split(',')) {
            val regex = match.getMatchReg()
            if (cartoonTitle.matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }

}

data class LocalPlayLine(
    val id: String,
    val label: String,
    val list: ArrayList<LocalEpisode> = arrayListOf(),
)

data class LocalEpisode(
    val label: String,
    val path: String,
)