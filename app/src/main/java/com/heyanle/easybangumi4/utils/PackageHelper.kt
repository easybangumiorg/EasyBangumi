package com.heyanle.easybangumi4.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import com.heyanle.easybangumi4.APP
import java.security.MessageDigest

/**
 * Created by heyanle on 2024/10/13
 * https://github.com/heyanLE
 */
object PackageHelper {

    val appSignature: String by lazy {
        getAppSignatures()
    }

    val appSignatureMD5: String by lazy {
        appSignature.getMD5()
    }

    fun getAppSignatures(packageName: String): List<String> {
        val signatures = mutableListOf<String>()
        try {
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                APP.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                APP.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signaturesArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            for (signature in signaturesArray) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val signatureBase64 = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                signatures.add(signatureBase64)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return signatures
    }

    fun getAppSignatures(): String {
        return getAppSignatures(APP.packageName).sorted().joinToString()
    }
}