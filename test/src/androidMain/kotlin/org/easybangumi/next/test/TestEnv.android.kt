package org.easybangumi.next.test

import org.easybangumi.next.Platform
import org.easybangumi.next.PlatformType
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual val platform: Module = module {
    single {
        object: Platform {
            override val platformType: PlatformType = PlatformType.Android
            override val platformName: String = PlatformType.Android.name
            override val isDebug: Boolean = true
            override val versionCode: Int = 99
            override val versionName: String = "99.99.99"
            override val sdkCode: Int = android.os.Build.VERSION.SDK_INT
        }
    }.binds(arrayOf(Platform::class))
}