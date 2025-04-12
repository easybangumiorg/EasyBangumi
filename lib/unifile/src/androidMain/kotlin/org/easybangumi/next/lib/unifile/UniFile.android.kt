package org.easybangumi.next.lib.unifile

import android.net.Uri
import okio.Path.Companion.toPath
import org.easybangumi.next.lib.unifile.core.OkioUniFile
import org.easybangumi.next.lib.utils.global
import java.io.File


typealias AUniFile = com.hippo.unifile.UniFile

actual fun UniFileFactory.fromUFD(ufd: UFD): UniFile? {
    when (ufd.type) {
        UFD.TYPE_JVM -> {
            JvmUniFile(File(ufd.uri))
        }
        UFD.TYPE_OKIO -> {
            OkioUniFile(path = ufd.uri.toPath())
        }
        UFD.TYPE_ANDROID_UNI -> {
            val context = global.appContext
            val uniFile = AUniFile.fromUri(context, Uri.parse(ufd.uri))
            return if (uniFile != null) {
                AUniFileWrapper(uniFile)
            } else {
                null
            }
        }
    }
    return null
}