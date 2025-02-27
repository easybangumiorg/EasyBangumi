package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.BaseModule
import com.heyanle.easy_bangumi_cm.preferences.PreferenceModule
import com.heyanle.easy_bangumi_cm.shared.Scheduler
import com.heyanle.lib.inject.core.Inject

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
        PreferenceModule().registerWith(Inject)
    }

    init {
        initBase()
        Scheduler.onInit()
    }

}