package com.heyanle.easybangumi4

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process

/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */
lateinit var APP: App

class App : Application() {

    init {
        Scheduler.runOnAppInit(this)
    }


    override fun onCreate() {
        super.onCreate()
        APP = this
        if (isMainProcess()) {
            Scheduler.runOnAppCreate(this)
        }


    }

    private fun isMainProcess(): Boolean {
        return packageName == if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            getProcessName(this) ?: packageName
        }

    }

    private fun getProcessName(cxt: Context): String? {
        val pid = Process.myPid()
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName
            }
        }
        return null
    }

}