package com.heyanle.easy_bangumi_cm.unifile.core

import android.content.Context
import android.net.Uri
import com.heyanle.easy_bangumi_cm.unifile.*
import com.heyanle.easy_bangumi_cm.unifile.core.contract.MediaContract
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by heyanlin on 2024/12/4.
 */
class MediaFile(
    ctx: Context,
    private val uri: Uri
): UniFile, TypeUniFile {

    private val context = ctx.applicationContext

    override fun createFile(displayName: String): UniFile? {
        return null
    }

    override fun createDirectory(displayName: String): UniFile? {
        return null
    }

    override fun getUri(): String {
        return uri.toString()
    }

    override fun getName(): String {
        return MediaContract.getName(context, uri) ?: ""
    }

    override fun getFilePath(): String {
        return MediaContract.getFilePath(context, uri) ?: ""
    }

    override fun getParentFile(): UniFile? {
        return null
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun isFile(): Boolean {
        val i: InputStream
        try {
            i = openInputStream()
        } catch (e: IOException) {
            return false
        }
        i.safeClose()
        return true
    }

    override fun lastModified(): Long {
        return MediaContract.lastModified(context, uri)
    }

    override fun lenght(): Long {
        return MediaContract.length(context, uri)
    }

    override fun canRead(): Boolean {
       return isFile()
    }

    override fun canWrite(): Boolean {
        val os: OutputStream
        try {
            os = openOutputStream(true)
        } catch (e: IOException) {
            return false
        }
        os.safeClose()
        return true
    }

    override fun delete(): Boolean {
        return false
    }

    override fun exists(): Boolean {
        return isFile()
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        return emptyArray()
    }

    override fun findFile(displayName: String): UniFile? {
        return null
    }

    override fun renameTo(displayName: String): Boolean {
        return false
    }

    override fun openOutputStream(append: Boolean): OutputStream {
        return TrickOutputStream.create(context, uri, if (append) "wa" else "w");
    }

    override fun openInputStream(): InputStream {
        return kotlin.runCatching {
            context.contentResolver.openInputStream(uri) ?: throw IOException("Can't open InputStream")
        }.getOrElse {
            throw IOException("Can't open InputStream")
        }
    }

    override fun getUniRandomAccessFile(mode: String): UniRandomAccessFile? {
        return context.contentResolver.openFileDescriptor(uri, mode)?.let {
            return RawRandomAccessFile(FDRandomAccessFile.from(it, mode) ?: return null)
        }
    }

    override fun getType(): String {
        return MediaContract.getType(context, uri) ?: super.getType()
    }
}