package com.heyanle.easy_bangumi_cm.repository.play

/**
 * Created by heyanlin on 2024/12/5.
 */
data class PlayInfo(
    val url: String,
    val type: String,
){

    companion object {
        const val TYPE_HLS = "video-hls"
        const val TYPE_NORMAL = "video-normal"
    }

}