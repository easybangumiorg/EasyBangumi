package org.easybangumi.next.lib.utils

import android.app.Application
import android.content.Context
import org.koin.mp.KoinPlatform

/**
 * Created by heyanlin on 2025/4/9.
 */
fun Global.getAppContext(): Context {
    return KoinPlatform.getKoin().get<Application>()
}