package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.BuildConfig


/**
 * Created by HeYanLe on 2024/12/3 0:28.
 * https://github.com/heyanLE
 */

class AndroidPlatform : Platform {
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