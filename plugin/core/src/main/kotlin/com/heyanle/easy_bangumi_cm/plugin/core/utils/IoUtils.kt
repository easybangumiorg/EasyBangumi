package com.heyanle.easy_bangumi_cm.plugin.core.utils

import java.io.InputStream

/**
 * Created by heyanlin on 2024/12/20.
 */
fun InputStream.readInt(): Int {
    val readBuffer = ByteArray(Int.SIZE_BYTES)
    this.read(readBuffer)
    return ((readBuffer[0].toInt() shl 24) +
            ((readBuffer[1].toInt() and 255) shl 16) +
            ((readBuffer[2].toInt() and 255) shl 8) +
            ((readBuffer[3].toInt() and 255) shl 0))
}