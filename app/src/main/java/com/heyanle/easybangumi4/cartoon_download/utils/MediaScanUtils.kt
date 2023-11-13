package com.heyanle.easybangumi4.cartoon_download.utils

import android.content.Context
import android.media.MediaScannerConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2023/10/3.
 */
object MediaScanUtils {

    suspend fun mediaScan(context: Context, path: String){
        suspendCoroutine<Unit> {
            MediaScannerConnection.scanFile(context, arrayOf(path), null
            ) { p0, p1 -> it.resume(Unit) }
        }

    }

}