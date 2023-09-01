package com.heyanle.easybangumi4.download

import android.content.Context
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import com.heyanle.easybangumi4.utils.getFilePath
import java.io.File

/**
 * Created by HeYanLe on 2023/9/2 0:14.
 * https://github.com/heyanLE
 */
class LocalCartoonController(
    private val context: Context
) {

    private val rootFile = context.getFilePath("download")

    fun getTargetFile(
        download: CartoonDownload
    ): File {
        TODO()
    }

}