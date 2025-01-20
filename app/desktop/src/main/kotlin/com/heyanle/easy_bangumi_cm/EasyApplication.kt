package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.shared.Scheduler
import com.heyanle.inject.core.Inject

/**
 * Created by heyanlin on 2024/12/3.
 */
class EasyApplication {

    companion object {
        lateinit var instance: EasyApplication
    }

    init {
        instance = this
    }


    // ================== InitBase ==================

    private fun initBase(){
        BaseModule().registerWith(Inject)
    }

    init {
        initBase()
        Scheduler.onInit()
    }

}