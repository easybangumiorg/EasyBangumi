package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.lib.inject.core.Inject

/**
 * Created by heyanlin on 2025/2/27.
 */

actual fun initHook() {
    PreferenceModule().registerWith(Inject)
}