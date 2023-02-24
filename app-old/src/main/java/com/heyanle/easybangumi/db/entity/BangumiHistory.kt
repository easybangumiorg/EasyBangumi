package com.heyanle.easybangumi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2023/1/29 21:17.
 * https://github.com/heyanLE
 */
@Entity
data class BangumiHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bangumiId: String,
    val name: String,
    val cover: String,
    val source: String,
    val detailUrl: String,
    val intro: String,
    val lastLinesIndex: Int,
    val lastEpisodeIndex: Int,
    val lastLineTitle: String,
    val lastEpisodeTitle: String,
    val lastProcessTime: Long,
    val createTime: Long,
)