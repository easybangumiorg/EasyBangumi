package com.heyanle.easy_bangumi_cm.shared.platform

import com.heyanle.easy_bangumi_cm.base.model.system.IPlatformInformation
import java.util.Properties

actual class PlatformInformation : IPlatformInformation {

    private val properties = Properties()

    private val _namespace: String by lazy {
        properties.getProperty("namespace") ?: throw Exception("desktop namespace is null")
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
        get() = "Android " + android.os.Build.VERSION.SDK_INT

    override val versionCode: Int
        get() = _versionCode

    override val versionName: String
        get() = _versionName

    override val isRelease: Boolean
        get() = _isRelease

    override val isAndroid: Boolean
        get() = true

    override val isIos: Boolean
        get() = false

    override val isDesktop: Boolean
        get() = false


    override fun toString(): String {
        return "PlatformInformation(namespace='$namespace', platformName='$platformName', versionCode=$versionCode, versionName='$versionName', isRelease=$isRelease, isAndroid=$isAndroid, isIos=$isIos, isDesktop=$isDesktop)"
    }
}