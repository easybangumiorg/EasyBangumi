package com.heyanle.easybangumi4.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import java.io.File
import com.heyanle.easybangumi4.R

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

    fun openAPK(filePath: String, context: Activity) {
        kotlin.runCatching {
            val file = File(filePath)
            if (!file.exists()) {
                return
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                stringRes(com.heyanle.easy_i18n.R.string.install_permissions_please).toast()
                ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.REQUEST_INSTALL_PACKAGES), 1)
                return
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkUri = FileProvider.getUriForFile(context, "com.heyanle.easybangumi4.fileProvider", file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        }.onFailure {
            it.printStackTrace()

        }
    }

}