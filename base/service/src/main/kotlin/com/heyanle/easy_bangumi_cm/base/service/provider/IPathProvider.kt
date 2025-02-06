package com.heyanle.easy_bangumi_cm.base.service.provider

interface IPathProvider {
    /**
     * 缓存路径
     */
    fun getCachePath(type: String): String

    /**
     * 文件路径
     */
    fun getFilePath(type: String): String
}