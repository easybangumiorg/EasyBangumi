package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.*
import com.heyanle.easy_bangumi_cm.shared.Scheduler
import com.heyanle.easy_bangumi_cm.shared.base.logger
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
        initBase()
        Scheduler.onInit()
    }


    // ================== InitBase ==================

    private val _logger = DesktopLogger()
    private fun initBase(){
        logger = _logger
        BaseModule().registerWith(Inject)
    }

}