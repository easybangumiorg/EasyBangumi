package com.heyanle.bangumi_source_api.api

/**
 * Created by HeYanLe on 2023/2/1 15:54.
 * https://github.com/heyanLE
 */
interface ParserLoader {

    fun load(): List<ISourceParser>

}