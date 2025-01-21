package com.heyanle.lib.unifile

import com.heyanle.lib.unifile.core.RawFile
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