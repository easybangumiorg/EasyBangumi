package com.heyanle.easybangumi.source.utils

import com.heyanle.bangumi_source_api.api.utils.PathUtils
import com.heyanle.easybangumi.BangumiApp

/**
 * Created by HeYanLe on 2023/2/1 17:25.
 * https://github.com/heyanLE
 */
class PathUtilsImpl : PathUtils {

    private val path : String by lazy {
        BangumiApp.INSTANCE.externalCacheDir!!.absolutePath
    }

    override fun getCachePath(): String {
        return path
    }
}