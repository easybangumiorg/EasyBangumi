package com.heyanle.easy_bangumi_cm

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.AndroidLogger
import com.heyanle.easy_bangumi_cm.shared.Global
import com.heyanle.easy_bangumi_cm.shared.Scheduler
import com.heyanle.easy_bangumi_cm.base.logger
import com.heyanle.inject.core.Inject


/**
 * Created by HeYanLe on 2024/12/3 0:13.
 * https://github.com/heyanLE
 */

class EasyApplication: Application() {

    companion object {
        lateinit var instance: EasyApplication
    }

    // ================== Application ==================

    override fun onCreate() {
        super.onCreate()
        instance = this
        Global.app = this
        initBase()
        initJavascript()
        Scheduler.onInit()
    }

    // ================== InitBase ==================

    private val _logger = AndroidLogger()
    private fun initBase(){
        logger = _logger
        BaseModule(this).registerWith(Inject)
    }

    private fun initJavascript(){
        AndroidJSMain.init()
    }
}