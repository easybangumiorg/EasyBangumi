package com.heyanle.lib.unifile.core.contract

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.heyanle.lib.unifile.NamedUri
import com.heyanle.lib.unifile.safeCloseNonRuntime


/**
 * Created by heyanlin on 2024/12/4.
 */
object DocumentsContractApi21 {

    const val PATH_DOCUMENT: String = "document"
    const val PATH_TREE: String = "tree"


    fun isTreeDocumentUri(context: Context, self: Uri): Boolean {
        if (DocumentsContractApi19.isContentUri(self) &&
            DocumentsContractApi19.isDocumentsProvider(context, self.authority)
        ) {
            val paths = self.pathSegments
            if (paths.size == 2) {
                return PATH_TREE == paths[0]
            } else if (paths.size == 4) {
                return PATH_TREE == paths[0] && PATH_DOCUMENT == paths[2]
            }
        }
        return false
    }

    fun createFile(
        context: Context,
        self: Uri,
        mimeType: String,
        displayName: String
    ): Uri? {
        try {
            return DocumentsContract.createDocument(
                context.contentResolver, self, mimeType,
                displayName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun createDirectory(
        context: Context,
        self: Uri,
        displayName: String
    ): Uri? {
        return createFile(context, self, DocumentsContract.Document.MIME_TYPE_DIR, displayName)
    }

    fun prepareTreeUri(treeUri: Uri): Uri {
        var documentId: String?
        try {
            documentId = DocumentsContract.getDocumentId(treeUri)
            requireNotNull(documentId)
        } catch (e: java.lang.Exception) {
            // IllegalArgumentException will be raised
            // if DocumentsContract.getDocumentId() failed.
            // But it isn't mentioned the document,
            // catch all kinds of Exception for safety.
            documentId = DocumentsContract.getTreeDocumentId(treeUri)
        }
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    }

    fun listFiles(context: Context, self: Uri): Array<Uri> {
        val resolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            self,
            DocumentsContract.getDocumentId(self)
        )
        val results = ArrayList<Uri>()

        var c: Cursor? = null
        try {
            c = resolver.query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
            if (null != c) {
                while (c.moveToNext()) {
                    val documentId = c.getString(0)
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                        self,
                        documentId
                    )
                    results.add(documentUri)
                }
            }
        } catch (e: java.lang.Exception) {
            // Log.w(TAG, "Failed query: " + e);
        } finally {
            c?.safeCloseNonRuntime()
        }

        return results.toTypedArray<Uri>()
    }

    fun listFilesNamed(context: Context, self: Uri): Array<NamedUri> {
        val resolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            self,
            DocumentsContract.getDocumentId(self)
        )
        val results: ArrayList<NamedUri> = ArrayList<NamedUri>()

        // Because SAF is slow, we use this method to also get the document's name with the id,
        // as performance isn't affected that much if used in this cursor. This saves from creating
        // a cursor every time "getName()" is called for every file.
        var c: Cursor? = null
        try {
            c = resolver.query(
                childrenUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ), null, null, null
            )
            if (null != c) {
                while (c.moveToNext()) {
                    val documentId = c.getString(0)
                    val documentName = c.getString(1)
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                        self,
                        documentId
                    )
                    results.add(NamedUri(documentUri, documentName))
                }
            }
        } catch (e: java.lang.Exception) {
            // Log.w(TAG, "Failed query: " + e);
        } finally {
            c?.safeCloseNonRuntime()
        }

        return results.toTypedArray<NamedUri>()
    }

    fun renameTo(context: Context, self: Uri, displayName: String): Uri? {
        try {
            return DocumentsContract.renameDocument(context.contentResolver, self, displayName)
        } catch (e: java.lang.Exception) {
            // Maybe user ejects tf card
            return null
        }
    }

    fun getName(context: Context, self: Uri): String {
        return Contract.queryForString(context, self, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null) ?: ""
    }
}