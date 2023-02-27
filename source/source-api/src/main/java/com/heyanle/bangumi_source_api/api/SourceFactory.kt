package com.heyanle.bangumi_source_api.api

/**
 * Created by HeYanLe on 2023/2/18 21:07.
 * https://github.com/heyanLE
 */
interface SourceFactory {

    fun create(): List<Source>

}