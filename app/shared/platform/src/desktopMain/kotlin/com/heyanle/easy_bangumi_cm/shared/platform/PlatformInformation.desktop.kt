package com.heyanle.easy_bangumi_cm.shared.platform

import com.heyanle.easy_bangumi_cm.shared.model.system.IPlatformInformation
import java.util.Properties

actual class PlatformInformation : IPlatformInformation {


    val hostOs: String by lazy {
        val osName = System.getProperty("os.name")
        when {
            osName == "Mac OS X" -> "macos"
            osName.startsWith("Win") -> "windows"
            osName == "Linux" -> "linux"
            else -> throw Error("Unknown OS $osName")
        }
    }

    val hostArch: String by lazy {
        val osArch = System.getProperty("os.arch")
        when (osArch) {
            "x86_64", "amd64" -> "x64"
            "aarch64" -> "arm64"
            else -> throw Error("Unknown arch $osArch")
        }
    }


    private val properties = Properties()
    private val _namespace: String by lazy {
        properties.getProperty("namespace") ?: throw Exception("desktop namespace is null")
    }

    private val _platformName: String by lazy {
        "Desktop ${hostOs} (${hostArch})"
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