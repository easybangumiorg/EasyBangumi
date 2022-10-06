package com.heyanle.easy_crasher

import android.content.Context
import android.content.Intent
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by HeYanLe on 2022/9/4 15:10.
 * https://github.com/heyanLE
 */
class CrashHandler(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        runCatching {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)
            var th: Throwable? = e.cause
            while (th != null) {
                th.printStackTrace(printWriter)
                th = th.cause
            }
            e.printStackTrace()
            val intent = Intent(context, CrashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra(CrashActivity.KEY_ERROR_MSG, stringWriter.toString())
            context.startActivity(intent)
        }.onFailure {
            it.printStackTrace()
        }
    }
}