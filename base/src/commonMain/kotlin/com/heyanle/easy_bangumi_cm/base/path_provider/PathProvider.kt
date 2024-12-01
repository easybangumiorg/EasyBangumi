package com.heyanle.easy_bangumi_cm.base.path_provider

import org.koin.core.module.Module


/**
 * 路径提供者
 * Created by HeYanLe on 2024/11/27 0:00.
 * https://github.com/heyanLE
 */
internal expect val pathProviderModule: Module
interface PathProvider {

    /**
     * 缓存路径
     */
    fun getCachePath(type: String): String

    /**
     * 文件路径
     */
    fun getFilePath(type: String): String

    /**
     * 用户选择的库路径
     */
    fun getLibraryPath(type: String): String

}