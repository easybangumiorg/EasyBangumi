package com.heyanle.easybangumi4.plugin.api



/**
 * Created by HeYanLe on 2023/10/18 22:39.
 * https://github.com/heyanLE
 */
interface SourceFactory {

    fun create(): List<Source>

}