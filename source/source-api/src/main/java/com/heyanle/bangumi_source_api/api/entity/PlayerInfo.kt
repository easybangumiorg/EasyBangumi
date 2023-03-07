package com.heyanle.bangumi_source_api.api.entity

class PlayerInfo(
    val decodeType: Int = DECODE_TYPE_OTHER,
    val uri: String = "",
) {
    companion object {
        // 这里跟 exoplayer 对应的类型需要对应

        const val DECODE_TYPE_DASH = 0
        const val DECODE_TYPE_HLS = 2
        const val DECODE_TYPE_OTHER = 4
    }
}