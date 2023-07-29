package com.heyanle.easybangumi4.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.heyanle.easybangumi4.compose.common.moeSnackBar

/**
 * Created by HeYanLe on 2023/5/17 16:04.
 * https://github.com/heyanLE
 */
object IntentHelper {

    fun openAppDetailed(packageName: String, context: Context){
        runCatching {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",packageName, null)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }.onFailure { "${it.loge().javaClass.simpleName}(${it.localizedMessage})".moeSnackBar() }
    }

}