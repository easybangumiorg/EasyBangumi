package com.heyanle.easy_bangumi_cm.shared.platform

import com.heyanle.easy_bangumi_cm.shared.model.system.IPlatformInformation
import com.heyanle.easy_bangumi_cm.BuildConfig

actual class PlatformInformation : IPlatformInformation {
    override val namespace: String
        get() = BuildConfig.APPLICATION_ID

    override val platformName: String
        get() = "Android " + android.os.Build.VERSION.SDK_INT

    override val versionCode: Int
        get() = BuildConfig.VERSION_CODE

    override val versionName: String
        get() = BuildConfig.VERSION_NAME

    override val isRelease: Boolean
        get() = !BuildConfig.DEBUG

    override val isAndroid: Boolean
        get() = true

    override val isIos: Boolean
        get() = false

    override val isDesktop: Boolean
        get() = false
}