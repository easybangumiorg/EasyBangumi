package com.heyanle.easybangumi4.cartoon.old.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by HeYanLe on 2023/8/6 16:12.
 * https://github.com/heyanLE
 */
@Entity(tableName = "CartoonTag")
data class CartoonTagOld(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val order: Int,
){

}