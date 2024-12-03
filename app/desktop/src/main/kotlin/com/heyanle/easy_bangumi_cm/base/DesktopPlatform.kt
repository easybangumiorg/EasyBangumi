package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.BaseException
import java.util.*


/**
 * Created by HeYanLe on 2024/12/3 0:45.
 * https://github.com/heyanLE
 */

class DesktopPlatform: Platform {

    val properties = Properties()
    private val _namespace: String by lazy {
        properties.getProperty("namespace") ?: throw BaseException("desktop namespace is null")
    }

    private val _platformName: String by lazy {
        properties.getProperty("platformName") ?: throw BaseException("desktop platformName is null")
    }

    private val _versionCode: Int by lazy {
        properties.getProperty("versionCode")?.toIntOrNull() ?: throw BaseException("desktop versionCode is null")
    }

    private val _versionName: String by lazy {
        properties.getProperty("versionName") ?: throw BaseException("desktop versionName is null")
    }

    private val _isRelease: Boolean by lazy {
        properties.getProperty("release") == "true"
    }



    override val namespace: String
        get() = _namespace

    override val platformName: String
        get() = _platformName

    override val versionCode: Int
        get() = _versionCode

    override val versionName: String
        get() = _versionName

    override val isRelease: Boolean
        get() = _isRelease

    override val isAndroid: Boolean
        get() = false

    override val isIos: Boolean
        get() = false

    override val isDesktop: Boolean
        get() = true
}