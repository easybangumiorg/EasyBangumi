package com.heyanle.lib.unifile

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


/**
 * Created by heyanlin on 2024/12/4.
 */
class TrickOutputStream(
    private val pfd: ParcelFileDescriptor,
    private val fd: FileDescriptor
): FileOutputStream(fd) {

    companion object {
        @Throws(IOException::class)
        fun create(context: Context, uri: Uri, mode: String): OutputStream {
            val pfd: ParcelFileDescriptor?
            try {
                pfd = context.contentResolver.openFileDescriptor(uri, mode)
            } catch (e: Exception) {
                throw IOException("Can't get ParcelFileDescriptor")
            }
            if (pfd == null) {
                throw IOException("Can't get ParcelFileDescriptor")
            }
            val fd = pfd.fileDescriptor ?: throw IOException("Can't get FileDescriptor")
            return TrickOutputStream(pfd, fd)
        }
    }



    override fun close() {
        pfd.close()
        super.close()
    }


}