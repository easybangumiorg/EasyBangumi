package com.heyanle.easy_bangumi_cm.unifile

import com.heyanle.easy_bangumi_cm.unifile.core.RawFile
import java.io.File
import java.net.URI

/**
 * Created by heyanlin on 2024/12/4.
 */

actual object UniFileFactory {

    actual fun fromFile(file: File): UniFile {
        return RawFile(null, file)
    }

    actual fun fromUri(uri: URI): UniFile? {
        if (uri.scheme != "file") {
            return null
        }
        return RawFile(null, File(uri))
    }

}