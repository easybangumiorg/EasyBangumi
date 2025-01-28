package com.heyanle.easy_bangumi_cm.common.theme

import android.os.Build
import com.heyanle.easy_bangumi_cm.base.model.system.IPlatformInformation

actual fun IPlatformInformation.isSupportDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}