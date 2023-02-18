package com.heyanle.bangumi_source_api.api2.play

class PlayerInfo(
    val decodeType: Int = DECODE_TYPE_OTHER,
    val uri: String = "",
) {
    companion object {
        const val DECODE_TYPE_DASH = 0
        const val DECODE_TYPE_HLS = 2
        const val DECODE_TYPE_OTHER = 4
    }
}