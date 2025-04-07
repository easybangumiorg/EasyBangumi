package com.heyanle.lib.unifile

import android.webkit.MimeTypeMap
import java.util.*


/**
 * Created by heyanlin on 2024/12/4.
 */
object AndroidUniUtils {

    fun getTypeForName(name: String): String {
        if (name.isEmpty()) {
            return ""
        }
        val lastDot = name.lastIndexOf('.')
        if (lastDot >= 0) {
            val extension = name.substring(lastDot + 1).lowercase(Locale.getDefault())
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            return mime?:""
        }
        return "application/octet-stream"
    }
}