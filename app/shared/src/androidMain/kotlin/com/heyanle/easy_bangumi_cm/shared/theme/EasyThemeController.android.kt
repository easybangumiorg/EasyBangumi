package com.heyanle.easy_bangumi_cm.shared.theme

import android.os.Build
import com.heyanle.easy_bangumi_cm.base.service.system.IPlatformInformation

actual fun IPlatformInformation.isSupportDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}