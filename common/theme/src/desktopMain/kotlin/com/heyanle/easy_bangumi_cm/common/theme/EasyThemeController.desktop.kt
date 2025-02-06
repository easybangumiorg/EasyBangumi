package com.heyanle.easy_bangumi_cm.common.theme

import com.heyanle.easy_bangumi_cm.base.service.system.IPlatformInformation

actual fun IPlatformInformation.isSupportDynamicColor(): Boolean {
    return false
}