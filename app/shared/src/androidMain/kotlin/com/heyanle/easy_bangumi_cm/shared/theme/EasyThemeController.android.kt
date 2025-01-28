package com.heyanle.easy_bangumi_cm.shared.theme

import android.os.Build
import com.heyanle.easy_bangumi_cm.shared.platform.PlatformInformation

actual fun PlatformInformation.isSupportDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}