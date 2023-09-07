package com.heyanle.easybangumi4.download.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2023/9/3 16:04.
 * https://github.com/heyanLE
 */
@Entity
data class DownloadCartoon(
    @PrimaryKey
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

){}