package com.heyanle.easy_bangumi_cm.shared.theme

import com.heyanle.easy_bangumi_cm.shared.platform.PlatformInformation

actual fun PlatformInformation.isSupportDynamicColor(): Boolean {
    return false
}