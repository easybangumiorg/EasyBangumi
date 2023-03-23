package com.heyanle.bangumi_source_api.api

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/18 21:07.
 * https://github.com/heyanLE
 */
@Keep
interface SourceFactory {

    fun create(): List<Source>

}