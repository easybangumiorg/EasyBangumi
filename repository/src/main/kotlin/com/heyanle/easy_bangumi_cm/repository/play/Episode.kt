package com.heyanle.easy_bangumi_cm.repository.play

/**
 * Created by heyanlin on 2024/12/5.
 */
data class Episode (
    val id: String,
    val label: String,
    val order: Int,
    val playInfo: PlayInfo,
){
    var ext: String = ""
}