package com.heyanle.easy_bangumi_cm.unifile.core

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.heyanle.easy_bangumi_cm.unifile.FDRandomAccessFile
import com.heyanle.easy_bangumi_cm.unifile.UniFile
import com.heyanle.easy_bangumi_cm.unifile.UniRandomAccessFile
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi19
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi21
import com.heyanle.easy_bangumi_cm.unifile.core.contract.DocumentsContractApi21.listFilesNamed
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by heyanlin on 2024/12/4.
 */
class TreeDocumentFile(
    private val parent: UniFile?,
    ctx: Context,
    private var uri: Uri,
    private var name: String = ""
) : UniFile, TypeUniFile {

    private val context = ctx.applicationContext

    override fun createFile(displayName: String): UniFile? {
        if (TextUtils.isEmpty(displayName)) {
            return null
        }

        val child = findFile(displayName)

        if (child != null) {
            if (child.isFile()) {
                return child
            } else {
//                Log.w(TAG, "Try to create file $displayName, but it is not file")
                return null
            }
        } else {
            // FIXME There's nothing about display name and extension mentioned in document.
            // But it works for com.android.externalstorage.documents.
            // The safest way is use application/octet-stream all the time,
            // But media store will not be updated.
            val index = displayName.lastIndexOf('.')
            if (index > 0) {
                val name = displayName.substring(0, index)
                val extension = displayName.substring(index + 1)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
                if (!TextUtils.isEmpty(mimeType)) {
                    val result = DocumentsContractApi21.createFile(context, uri, mimeType, name)
                    return if (result != null) TreeDocumentFile(this, context, result) else null
                }
            }

            // Not dot in displayName or dot is the first char or can't get MimeType
            val result = DocumentsContractApi21.createFile(context, uri, "application/octet-stream", displayName)
            return if (result != null) TreeDocumentFile(this, context, result) else null
        }
    }

    override fun createDirectory(displayName: String): UniFile? {
        if (TextUtils.isEmpty(displayName)) {
            return null
        }

        val child = findFile(displayName)

        if (child != null) {
            return if (child.isDirectory()) {
                child
            } else {
                null
            }
        } else {
            val result = DocumentsContractApi21.createDirectory(context, uri, displayName)
            return if (result != null) TreeDocumentFile(this, context, result) else null
        }
    }

    override fun getUri(): String {
        return uri.toString()
    }

    override fun getName(): String {
        if (name.isEmpty()) {
            name = DocumentsContractApi19.getName(context, uri);
        }
        return name
    }

    override fun getFilePath(): String {
        return DocumentsContractApi19.getFilePath(context, uri) ?: ""
    }

    override fun getParentFile(): UniFile? {
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
        return if (isDirectory()) -1L else DocumentsContractApi19.length(context, uri)
    }

    override fun canRead(): Boolean {
        return DocumentsContractApi19.canRead(context, uri)
    }

    override fun canWrite(): Boolean {
        return DocumentsContractApi19.canWrite(context, uri)
    }

    override fun delete(): Boolean {
        name = ""
        return DocumentsContractApi19.delete(context, uri)
    }

    override fun exists(): Boolean {
        return DocumentsContractApi19.exists(context, uri)
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        if (!isDirectory()) {
            return emptyArray()
        }

        val result = listFilesNamed(context, uri)
        val resultFiles = kotlin.arrayOfNulls<UniFile>(result.size)

        var i = 0
        val n = result.size
        while (i < n) {
            val namedUri = result[i]
            if (filter != null && !filter(this, namedUri.name)) {
                i++
                continue
            }
            resultFiles[i] = TreeDocumentFile(this, context, namedUri.uri, namedUri.name)
            i++
        }

        return resultFiles
    }

    override fun findFile(displayName: String): UniFile? {
        if (TextUtils.isEmpty(displayName)) {
            return null
        }

        if (!isDirectory()) {
            return null
        }


        // This implementation assumes that document IDs are formed
        // based on filenames, which is a reasonable assumption for
        // most document providers, but is not guaranteed by the spec.
        //
        // Without making the assumption that document IDs are
        // arranged in a reasonable way, it is impossible to check for
        // file existence in a way that is not extremely slow.
        //
        // If it turns out that some popular devices use a document
        // provider for which this is a bad assumption, then we should
        // revisit this implementation and perhaps special-case a
        // fallback to the slow way for those providers. It's possible
        // to check the name of the document provider by using similar
        // code to DocumentsContractApi19.isDocumentsProvider to
        // identify which provider is being used, and then print the
        // name of the class.
        //
        // Note on case sensitivity: this method should always behave
        // correctly with respect to the case sensitivity of the
        // filesystem. That is, on a case-sensitive filesystem it will
        // do a case-sensitive existence check, while on a
        // case-insensitive filesystem (case-preserving or not) it
        // will implicitly do a case-insensitive existence check.
        // Previous versions of this method took an explicit parameter
        // for whether the check should be case sensitive or not, but
        // that should no longer be necessary with the current
        // implementation.
        var documentId = DocumentsContract.getDocumentId(uri)
        documentId += "/$displayName"

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
        val child: UniFile = TreeDocumentFile(this, context, documentUri, displayName)

        return if (child.exists()) {
            child
        } else {
            null
        }
    }

    override fun renameTo(displayName: String): Boolean {
        name = ""
        val result = DocumentsContractApi21.renameTo(context, uri, displayName);
        if (result != null) {
            uri = result;
            return true;
        } else {
            return false;
        }
    }

    override fun openOutputStream(append: Boolean): OutputStream {
        if (isDirectory()) {
            throw  IOException("Cannot open an output stream to a directory: $uri")
        }
        return context.contentResolver.openOutputStream(uri, if (append) "wa" else "w")!!
    }

    override fun openInputStream(): InputStream {
        if (isDirectory()) {
            throw IOException("Cannot open an input stream to a directory: $uri")
        }
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