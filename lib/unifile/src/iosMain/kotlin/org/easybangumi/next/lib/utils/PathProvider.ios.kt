package org.easybangumi.next.lib.utils

import org.easybangumi.next.lib.unifile.UFD

actual interface PathProvider {
    actual fun getFilePath(type: String): UFD
    actual fun getCachePath(type: String): UFD
}

actual val pathProvider: PathProvider
    get() = TODO("Not yet implemented")