package com.heyanle.easybangumi4.download.entity

import com.arialyy.aria.core.download.M3U8Entity
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/9/17 15:40.
 * https://github.com/heyanLE
 */
data class DownloadItem(
    val uuid: String,
    // cartoon 外键
    val cartoonId: String,
    val cartoonUrl: String,
    val cartoonSource: String,

    // 展示数据
    val cartoonTitle: String,
    val cartoonCover: String,
    val cartoonDescription: String,
    val cartoonGenre: String,

    val playLine: PlayLine,

    val episodeLabel: String,
    val episodeIndex: Int,

    val state: Int, // -1 -> error, 1 -> parsing , 2 -> ariaing , 3 -> transcoding， 4 -> completely

    val playerInfo: PlayerInfo? = null,

    val ariaId: Long = -1,
    val m3U8Entity: M3U8Entity? = null,

    val errorMsg: String = "",

    val filePathWithoutSuffix: String,
) {
}