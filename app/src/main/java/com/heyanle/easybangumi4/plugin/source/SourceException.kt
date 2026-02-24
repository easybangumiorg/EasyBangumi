package com.heyanle.easybangumi4.plugin.source

/**
 * Created by HeYanLe on 2023/10/29 18:11.
 * https://github.com/heyanLE
 */
class SourceException(
    val msg: String,
    var exception: Exception? = null
): Exception(msg) {
}