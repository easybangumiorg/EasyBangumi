package org.easybangumi.next

import android.app.Application
import org.easybangumi.next.crash.CrashHandler
import org.easybangumi.next.shared.Scheduler
import javax.net.ssl.HttpsURLConnection

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class EasyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        Android.onInit(this)
    }


}