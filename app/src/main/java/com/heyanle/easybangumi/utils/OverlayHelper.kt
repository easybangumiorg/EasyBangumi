package com.heyanle.easybangumi.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

/**
 * Created by HeYanLe on 2023/1/12 22:01.
 * https://github.com/heyanLE
 */
object OverlayHelper {

    fun drawOverlayEnable(context: Context): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            // 无法判断，这里需要引导用户自己开启
            true
        }
    }

    fun getWindowManager(context: Context): WindowManager {
        return context.getSystemService(Application.WINDOW_SERVICE) as WindowManager
    }

}