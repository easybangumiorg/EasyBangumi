package com.heyanle.easy_bangumi_cm

import org.koin.core.Koin
import org.koin.mp.KoinPlatformTools


/**
 * Created by HeYanLe on 2024/12/3 1:18.
 * https://github.com/heyanLE
 */

val koin: Koin by lazy {
    KoinPlatformTools.defaultContext().get()
}