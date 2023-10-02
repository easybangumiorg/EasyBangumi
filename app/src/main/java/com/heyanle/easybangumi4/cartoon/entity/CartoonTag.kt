package com.heyanle.easybangumi4.cartoon.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2023/8/6 16:12.
 * https://github.com/heyanLE
 */
@Entity
data class CartoonTag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val order: Int,
)