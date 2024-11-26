package com.heyanle.easy_bangumi_cm.component.provider.path


/**
 * 路径提供者
 * Created by HeYanLe on 2024/11/27 0:00.
 * https://github.com/heyanLE
 */

interface PathProvider {

    fun getCachePath(type: String): String

    fun getFilePath(type: String): String

    fun getLibraryPath(type: String): String

}