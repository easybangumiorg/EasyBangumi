package com.heyanle.easybangumi.utils

import android.util.Log
import android.widget.Toast
import com.heyanle.easybangumi.BuildConfig
import com.heyanle.easybangumi.EasyApplication

/**
 * Created by HeYanLe on 2021/11/21 14:04.
 * https://github.com/heyanLE
 */
fun String.logI(tag: String){
    Log.i(tag, this)
}

fun String.logE(tag: String){
    Log.e(tag, this)
}

fun String.toastWithDebug(){
    if(BuildConfig.DEBUG){
        Toast.makeText(EasyApplication.INSTANCE, this, Toast.LENGTH_SHORT).show()
    }
}