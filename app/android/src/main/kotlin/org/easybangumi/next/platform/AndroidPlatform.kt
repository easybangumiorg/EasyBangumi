package org.easybangumi.next.platform

import org.easybangumi.next.BuildConfig
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

object AndroidPlatform: Platform {

    override val sdkCode: Int = android.os.Build.VERSION.SDK_INT
    override val platformType: PlatformType = PlatformType.Android
    override val platformName: String = "Android $sdkCode"
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val versionCode: Int = BuildConfig.VERSION_CODE
    override val versionName: String = BuildConfig.VERSION_NAME

}