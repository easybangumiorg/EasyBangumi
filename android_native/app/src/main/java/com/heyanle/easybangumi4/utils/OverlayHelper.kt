package com.heyanle.easybangumi4.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager


/**
 * Created by HeYanLe on 2023/1/12 22:01.
 * https://github.com/heyanLE
 */
object OverlayHelper {

    fun drawOverlayEnable(context: Context): Boolean {
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

    /**
     * 跳转到授予悬浮球权限
     * @param context           上下文对象
     */
    fun gotoDrawOverlaySetting(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        } else {
            gotoAppDetailSetting(context)
        }
        //Toast.makeText(context, com.heyanle.easy_i18n.R.string.please_allow_draw_overlay,Toast.LENGTH_SHORT).show();
    }

    /**
     * 跳转到应用详情界面
     * @param context           上下文对象
     */
    fun gotoAppDetailSetting(context: Context) {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        localIntent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(localIntent)
    }

}