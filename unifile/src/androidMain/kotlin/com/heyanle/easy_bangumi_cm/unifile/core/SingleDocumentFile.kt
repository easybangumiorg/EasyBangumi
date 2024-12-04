package com.heyanle.easy_bangumi_cm.unifile.core

import android.content.Context
import android.net.Uri
import com.heyanle.easy_bangumi_cm.unifile.FDRandomAccessFile
import com.heyanle.easy_bangumi_cm.unifile.IUniFile
import com.heyanle.easy_bangumi_cm.unifile.UniRandomAccessFile
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi19
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi21
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by heyanlin on 2024/12/4.
 */
class SingleDocumentFile(
    private val parent: IUniFile?,
    ctx: Context,
    private val uri: Uri,
) : IUniFile, TypeIUniFile {

    private val context = ctx.applicationContext

    override fun createFile(displayName: String): IUniFile? {
        return null
    }

    override fun createDirectory(displayName: String): IUniFile? {
        return null
    }

    override fun getUri(): String {
        return uri.toString()
    }

    override fun getName(): String {
        return DocumentsContractApi21.getName(context, uri) ?: ""
    }

    override fun getFilePath(): String {
        return DocumentsContractApi19.getFilePath(context, uri) ?: ""
    }

    override fun getParentFile(): IUniFile? {
        return parent
    }

    override fun isDirectory(): Boolean {
        return DocumentsContractApi19.isDirectory(context, uri)
    }

    override fun isFile(): Boolean {
        return DocumentsContractApi19.isFile(context, uri)
    }

    override fun lastModified(): Long {
        return DocumentsContractApi19.lastModified(context, uri)
    }

    override fun lenght(): Long {
        return DocumentsContractApi19.length(context, uri)
    }

    override fun canRead(): Boolean {
        return DocumentsContractApi19.canRead(context, uri)
    }

    override fun canWrite(): Boolean {
        return DocumentsContractApi19.canWrite(context, uri)
    }

    override fun delete(): Boolean {
        return DocumentsContractApi19.delete(context, uri)
    }

    override fun exists(): Boolean {
        return DocumentsContractApi19.exists(context, uri)
    }

    override fun listFiles(filter: ((IUniFile, String) -> Boolean)?): Array<IUniFile?> {
        return emptyArray()
    }

    override fun findFile(displayName: String): IUniFile? {
        return null
    }

    override fun renameTo(displayName: String): Boolean {
        return false
    }

    override fun openOutputStream(append: Boolean): OutputStream {
        return context.contentResolver.openOutputStream(uri, if (append) "wa" else "w") ?: throw IOException("open output stream failed")
    }

    override fun openInputStream(): InputStream {
        return context.contentResolver.openInputStream(uri) ?: throw IOException("open input stream failed")
    }

    override fun getUniRandomAccessFile(mode: String): UniRandomAccessFile? {
        // Check file
        if (!isFile()) {
            throw IOException("Can't make sure it is file");
        }
        val pfd = context.contentResolver.openFileDescriptor(uri, mode) ?: return null;
        return RawRandomAccessFile(FDRandomAccessFile.from(pfd, mode) ?: return null)
    }

    override fun getType(): String {
        return DocumentsContractApi19.getType(context, uri) ?: super.getType()
    }
}