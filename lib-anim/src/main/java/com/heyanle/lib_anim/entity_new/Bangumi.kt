package com.heyanle.lib_anim.entity_new

/**
 * Created by HeYanLe on 2023/1/20 0:50.
 * https://github.com/heyanLE
 */
data class Bangumi (
    val name: String,
    val cover: String,
    val description: String,
    val intro: String,
    val tags: List<String>, // 逗号分开
    val isFinish: Boolean?, // 是否完结， null 为未知
    val sourceKey: String,
    val id: String,
)