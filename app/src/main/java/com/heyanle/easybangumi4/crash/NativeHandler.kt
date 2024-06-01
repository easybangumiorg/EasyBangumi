package com.heyanle.easybangumi4.crash

import android.content.Context
import com.pika.lib_signal.CallOnCatchSignal

/**
 * Created by heyanle on 2024/5/31.
 * https://github.com/heyanLE
 */
class NativeHandler: CallOnCatchSignal {

    override fun checkIsAnr(): Boolean {
        return false
    }

    override fun handleAnr(context: Context, logcat: String) {

    }

    override fun handleCrash(context: Context, signal: Int, logcat: String) {
        SourceCrashController.onNativeCrash(signal, logcat)
    }
}