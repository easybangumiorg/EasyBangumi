package com.heyanle.easybangumi4.utils

import android.os.Looper

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
fun isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}