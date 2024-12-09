package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.base.Platform
import org.jetbrains.skiko.hostArch
import org.jetbrains.skiko.hostOs
import java.util.*


/**
 * Created by HeYanLe on 2024/12/3 0:45.
 * https://github.com/heyanLE
 */

class DesktopPlatform: Platform {

    private val properties = Properties()
    private val _namespace: String by lazy {
        properties.getProperty("namespace") ?: throw Exception("desktop namespace is null")
    }

    private val _platformName: String by lazy {
        "Desktop ${hostOs.name} ${hostArch.name}"
    }

    private val _versionCode: Int by lazy {
        properties.getProperty("versionCode")?.toIntOrNull() ?: throw Exception("desktop versionCode is null")
    }

    private val _versionName: String by lazy {
        properties.getProperty("versionName") ?: throw Exception("desktop versionName is null")
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