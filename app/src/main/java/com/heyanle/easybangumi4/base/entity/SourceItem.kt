package com.heyanle.easybangumi4.base.entity

import androidx.room.Entity

/**
 * Created by HeYanLe on 2023/7/30 13:06.
 * https://github.com/heyanLE
 */
@Entity(primaryKeys = ["key"])
data class SourceItem(
    val key: String,
    val order: Int,
    val enable: Boolean
)