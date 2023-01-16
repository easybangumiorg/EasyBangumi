package com.heyanle.easybangumi.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Entity
data class SearchHistory (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val content: String
)