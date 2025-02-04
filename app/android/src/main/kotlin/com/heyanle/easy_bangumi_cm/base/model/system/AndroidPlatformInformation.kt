package com.heyanle.easy_bangumi_cm.base.model.system

import android.content.Context
import com.heyanle.easy_bangumi_cm.BuildConfig


/**
 * Created by HeYanLe on 2025/2/4 17:42.
 * https://github.com/heyanLE
 */

class AndroidPlatformInformation(
    private val context: Context
): IPlatformInformation {

    override val namespace: String by lazy{
        context.packageName
    }

    override val platformName: String by lazy {
        "Android " + android.os.Build.VERSION.SDK_INT
    }

    override val versionCode: Int by lazy {
        BuildConfig.VERSION_CODE
    }

    override val versionName: String by lazy {
        BuildConfig.VERSION_NAME
    }
    override val isRelease: Boolean by lazy {
        !BuildConfig.DEBUG
    }
    override val isAndroid: Boolean = true
    override val isIos: Boolean = false
    override val isDesktop: Boolean = false

    override fun toString(): String {
        return "PlatformInformation(namespace='$namespace', platformName='$platformName', versionCode=$versionCode, versionName='$versionName', isRelease=$isRelease, isAndroid=$isAndroid, isIos=$isIos, isDesktop=$isDesktop)"
    }
}