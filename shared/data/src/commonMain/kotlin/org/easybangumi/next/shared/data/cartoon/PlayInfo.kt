package org.easybangumi.next.shared.data.cartoon

/**
 * Created by heyanle on 2024/12/5.
 */
data class PlayInfo(
    val url: String,
    val type: String,
){

    var header: Map<String, String>? = null

    companion object {
        const val TYPE_HLS = "video-hls"
        const val TYPE_NORMAL = "video-normal"

        //TODO
        const val TYPE_BT_URL = "video-bt-url"
        const val TYPE_BT_MAGNET = "video-bt-magnet"
    }

}