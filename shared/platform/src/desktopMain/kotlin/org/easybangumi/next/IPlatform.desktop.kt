package org.easybangumi.next

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

object PlatformInformation : IPlatformInformation {

    const val OS_MAC = "macos"
    const val OS_WINDOWS = "windows"
    const val OS_LINUX = "linux"

    const val ARCH_X64 = "x64"

    const val ARCH_ARM = "arm"
    const val ARCH_AARCH = "aarch"


    override val hostOs: String by lazy {
        val osName = System.getProperty("os.name")
        when {
            "mac" in osName || "os x" in osName || "darwin" in osName -> OS_MAC
            "windows" in osName -> OS_WINDOWS
            "linux" in osName -> OS_LINUX
            else -> throw Error("Unknown OS $osName")
        }
    }

    override val hostArch: String by lazy {
        when (val osArch = System.getProperty("os.arch")) {
            "x86_64", "amd64" -> ARCH_X64
            "aarch64" -> ARCH_AARCH
            "arm64" -> ARCH_ARM
            else -> throw Error("Unknown arch $osArch")
        }
    }

    override val platformName: String
        get() = "Desktop ${hostOs} (${hostArch})"

    override val isAndroid: Boolean
        get() = false

    override val isIos: Boolean
        get() = false

    override val isDesktop: Boolean
        get() = true
}

actual val platformInformation: IPlatformInformation
    get() = PlatformInformation


actual interface IPlatformInformation {
    actual val platformName: String
    actual val isAndroid: Boolean
    actual val isDesktop: Boolean
    actual val isIos: Boolean

    val hostOs: String
    val hostArch: String

}