package com.heyanle.easybangumi.crash

import android.content.Context
import android.content.Intent
import com.heyanle.easybangumi.utils.start
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by HeYanLe on 2021/9/12 15:30.
 * https://github.com/heyanLE
 */
class CrashHandler(
    private val context: Context
    ) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thr: Thread,e: Throwable) {
        runCatching {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)
            var th: Throwable? = e.cause
            while (th != null) {
                th.printStackTrace(printWriter)
                th = th.cause
            }

            context.start<CrashActivity> {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(CrashActivity.INTENT_KEY, stringWriter.toString())
            }
            e.printStackTrace()
        }.onFailure {
            it.printStackTrace()
        }




    }
}