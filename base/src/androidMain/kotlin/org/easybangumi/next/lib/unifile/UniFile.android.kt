package org.easybangumi.next.lib.unifile

import android.net.Uri
import okio.Path.Companion.toPath
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.core.OkioUniFile
import org.easybangumi.next.lib.utils.global
import java.io.File


typealias AUniFile = com.hippo.unifile.UniFile

private val logger = logger("UniFile")

actual fun UniFileFactory.fromUFD(ufd: UFD): UniFile? {
    logger.info(ufd.toString())
    return when (ufd.type) {
        UFD.TYPE_JVM -> {
            JvmUniFile(File(ufd.uri))
        }
        UFD.TYPE_OKIO -> {
            OkioUniFile(path = ufd.uri.toPath())
        }
        UFD.TYPE_ANDROID_UNI -> {
            val context = global.appContext
            val uniFile = AUniFile.fromUri(context, Uri.parse(ufd.uri))
            if (uniFile != null) {
                AUniFileWrapper(uniFile)
            } else {
                null
            }
        }
        UFD.TYPE_ASSETS -> {
            val context = global.appContext
            val assetManager = context.assets ?: return null
            val uniFile = AUniFile.fromAsset(assetManager, ufd.uri)
            if (uniFile != null) {
                AUniFileWrapper(uniFile)
            } else {
                null
            }
        }
        else -> null
    }
}