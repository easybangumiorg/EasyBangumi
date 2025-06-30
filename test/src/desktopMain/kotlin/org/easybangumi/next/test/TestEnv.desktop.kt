package org.easybangumi.next.test

import org.easybangumi.next.DesktopHostArch
import org.easybangumi.next.DesktopHostOs
import org.easybangumi.next.Platform
import org.easybangumi.next.PlatformType
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual val platform: Module = module {
    single {
        object: Platform {
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


            override val isDebug: Boolean = true

            override val versionCode: Int = 99

            override val versionName: String = "99.99.99"

            override val platformType: PlatformType = PlatformType.Desktop
        }
    }.binds(arrayOf(Platform::class))
}