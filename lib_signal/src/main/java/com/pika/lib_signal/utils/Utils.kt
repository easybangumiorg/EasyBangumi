package com.pika.lib_signal.utils

import android.os.Build
import android.os.Process
import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Utils {
    fun getLogcat(logcatMainLines: Int, logcatSystemLines: Int, logcatEventsLines: Int): String {
        val pid = Process.myPid()
        val sb = StringBuilder()
        sb.append("logcat-----------------------------:\n")
        if (logcatMainLines > 0) {
            getLogcatByBufferName(pid, sb, "main", logcatMainLines, 'D')
        }
        if (logcatSystemLines > 0) {
            getLogcatByBufferName(pid, sb, "system", logcatSystemLines, 'W')
        }
        if (logcatEventsLines > 0) {
            getLogcatByBufferName(pid, sb, "events", logcatSystemLines, 'I')
        }
        sb.append("\n")
        sb.append("logcat end ---------------------------:\n")
        return sb.toString()
    }

    private fun getLogcatByBufferName(
        pid: Int,
        sb: StringBuilder,
        bufferName: String,
        lines: Int,
        priority: Char
    ) {
        val withPid = Build.VERSION.SDK_INT >= 24
        val pidString = Integer.toString(pid)
        val pidLabel = " $pidString "

        //command for ProcessBuilder
        val command: MutableList<String> = ArrayList()
        command.add("/system/bin/logcat")
        command.add("-b")
        command.add(bufferName)
        command.add("-d")
        command.add("-v")
        command.add("threadtime")
        command.add("-t")
        command.add(Integer.toString(if (withPid) lines else (lines * 1.2).toInt()))
        if (withPid) {
            command.add("--pid")
            command.add(pidString)
        }
        command.add("*:$priority")

        //append the command line
        val commandArray: Array<Any> = command.toTypedArray()
        sb.append("--------- tail end of log ").append(bufferName)
        sb.append(" (").append(TextUtils.join(" ", commandArray)).append(")\n")

        //append logs
        var br: BufferedReader? = null
        var line: String
        try {
            val process = ProcessBuilder().command(command).start()
            br = BufferedReader(InputStreamReader(process.inputStream))
            while (br.readLine().also { line = it } != null) {
                if (withPid || line.contains(pidLabel)) {
                    sb.append(line).append("\n")
                }
            }
        } catch (e: Exception) {
           e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }

    fun getStacktraceForMainThread(): String {
        val target = Thread.getAllStackTraces().entries.find {
            it.key.name == "main"
        }
        val stringBuilder = StringBuilder()
        target?.value?.forEach {
            stringBuilder.append("    at ").append(it.toString()).append("\n");
        }
        return stringBuilder.toString()
    }

}