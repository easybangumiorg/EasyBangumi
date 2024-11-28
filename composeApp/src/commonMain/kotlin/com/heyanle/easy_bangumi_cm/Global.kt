package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.initBase
import com.heyanle.easy_bangumi_cm.media.initMediaModule
import org.koin.core.Koin
import org.koin.core.context.KoinContext
import org.koin.mp.KoinPlatformTools


/**
 * Created by HeYanLe on 2024/11/27 1:08.
 * https://github.com/heyanLE
 */

val koin: Koin by lazy {
    KoinPlatformTools.defaultContext().get()
}

object Global {

    fun onInit(){
        initBase()
        initMediaModule()
    }

}
