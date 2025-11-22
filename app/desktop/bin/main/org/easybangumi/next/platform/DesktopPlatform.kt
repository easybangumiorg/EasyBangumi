package org.easybangumi.next.platform

import org.easybangumi.next.DesktopHostArch
import org.easybangumi.next.DesktopHostOs
import org.easybangumi.next.EasyConfig
import org.easybangumi.next.Platform
import org.easybangumi.next.PlatformType

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

object DesktopPlatform : Platform {

    override val hostOs: DesktopHostOs by lazy {
        val osName = System.getProperty("os.name").lowercase()
        when {
            "mac" in osName || "os x" in osName || "darwin" in osName -> DesktopHostOs.MacOS
            "windows" in osName -> DesktopHostOs.Windows
            "linux" in osName -> DesktopHostOs.Linux
            else -> throw Error("Unknown OS $osName")
        }
    }

    override val hostArch: DesktopHostArch by lazy {
        val osArch = System.getProperty("os.arch").lowercase()
        when  {
            // 暂不支持 x86 架构
            "x86_64" in osArch || "amd64" in osArch -> DesktopHostArch.X64
            "aarch64" in osArch || "arm64" in osArch -> DesktopHostArch.ARM64
            else -> throw Error("Unknown arch $osArch")
        }
    }

    override val platformName: String
        get() = "${hostOs.name}-${hostArch.name}"


    override val isDebug: Boolean = EasyConfig.IS_DEBUG

    override val versionCode: Int = EasyConfig.VERSION_CODE

    override val versionName: String = EasyConfig.VERSION_NAME

    override val platformType: PlatformType = PlatformType.Desktop
}