package com.pika.lib_signal

import android.app.Application
import android.os.*
import android.util.Log
import com.pika.lib_signal.utils.Utils
import com.pika.lib_signal.utils.Utils.getStacktraceForMainThread

/**
 * author : TestPlanB
 */


object SignalController {
    const val TAG = "hi_signal"
    private var application: Application? = null

    private var callOnCatchSignal: CallOnCatchSignal? = null

    init {
        System.loadLibrary("keep-signal")
    }

    @JvmStatic
    fun signalError() {
        throw SignalException()
    }


    @JvmStatic
    fun callNativeException(signal: Int, nativeStackTrace: String) {
        Log.i(TAG, "callNativeException $signal")
        //Log.i(TAG, getStacktraceForMainThread())

        // 处理anr的场景
        if(signal == SignalConst.SIGQUIT && callOnCatchSignal?.checkIsAnr() == true){
            application?.let {
                callOnCatchSignal?.handleAnr(it,Utils.getLogcat(20,20,20))
            }
            return
        }
        application?.let {
            callOnCatchSignal?.handleCrash(it,signal,Utils.getLogcat(20,20,20))
        }



    }


    @JvmStatic
    fun initSignal(signals: IntArray, application: Application,callOnCatchSignal: CallOnCatchSignal) {
        SignalController.application = application
        SignalController.callOnCatchSignal = callOnCatchSignal
        initWithSignals(signals)
    }

    @JvmStatic
    private external fun initWithSignals(signals: IntArray)



}
