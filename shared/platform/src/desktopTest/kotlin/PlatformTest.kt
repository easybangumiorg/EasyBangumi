import org.easybangumi.next.DesktopHostArch
import org.easybangumi.next.DesktopHostOs
import org.easybangumi.next.Platform
import org.easybangumi.next.PlatformType
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.binds
import org.koin.dsl.module

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

fun injectTestPlatform() {
    startKoin {
        modules(module {
            single {
                object: Platform {
                    override val platformType: PlatformType
                        get() = PlatformType.Desktop
                    override val platformName: String
                        get() = PlatformType.Desktop.name
                    override val isDebug: Boolean = true
                    override val versionCode: Int = 1
                    override val versionName: String = "1.0.0"
                    override val hostOs: DesktopHostOs = DesktopHostOs.Windows
                    override val hostArch: DesktopHostArch = DesktopHostArch.X64
                }
            }.binds(arrayOf(Platform::class))
        })
    }
}

class PlatformTest {
}