package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.inject.core.Inject

/**
 * 初始化时点分发
 * Created by heyanlin on 2024/12/3.
 */

object Scheduler {

    fun onInit() {
        SharedModule().registerWith(Inject)
    }

    fun onSplashPageLaunch(){

    }

    fun onHomePageLaunch(){

    }


}

