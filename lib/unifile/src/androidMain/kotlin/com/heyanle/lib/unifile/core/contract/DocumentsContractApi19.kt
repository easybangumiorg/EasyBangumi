package com.heyanle.lib.unifile.core.contract

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import com.heyanle.lib.unifile.safeCloseNonRuntime


/**
 * Created by heyanlin on 2024/12/4.
 */
object DocumentsContractApi19 {


    const val AUTHORITY_DOCUMENT_EXTERNAL_STORAGE: String = "com.android.externalstorage.documents"
    const val AUTHORITY_DOCUMENT_DOWNLOAD: String = "com.android.providers.downloads.documents"
    const val AUTHORITY_DOCUMENT_MEDIA: String = "com.android.providers.media.documents"
    const val PROVIDER_INTERFACE: String = "android.content.action.DOCUMENTS_PROVIDER"
    const val PATH_DOCUMENT: String = "document"
    const val PATH_TREE: String = "tree"

    fun isContentUri(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT == uri.scheme
    }

    fun isDocumentsProvider(context: Context, authority: String?): Boolean {
        val intent: Intent = Intent(DocumentsContract.PROVIDER_INTERFACE)
        val infos = context.packageManager
            .queryIntentContentProviders(intent, 0)
        for (info in infos) {
            if (authority == info.providerInfo.authority) {
                return true
            }
        }
        return false
    }

    // It is different from DocumentsContract.isDocumentUri().
    // It accepts uri like content://com.android.externalstorage.documents/tree/primary%3AHaHa as well.
    fun isDocumentUri(context: Context, self: Uri): Boolean {
        if (isContentUri(self) && isDocumentsProvider(context, self.authority)) {
            val paths = self.pathSegments
            if (paths.size == 2) {
                return PATH_DOCUMENT == paths[0] || PATH_TREE == paths[0]
            } else if (paths.size == 4) {
                return PATH_TREE == paths[0] && PATH_DOCUMENT == paths[2]
            }
        }
        return false
    }

    fun getName(context: Context, self: Uri): String {
        return Contract.queryForString(context, self, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null) ?: ""
    }

    private fun getRawType(context: Context, self: Uri): String {
        return Contract.queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE, null) ?: ""
    }

    fun getType(context: Context, self: Uri): String? {
        val rawType = getRawType(context, self)
        return if (DocumentsContract.Document.MIME_TYPE_DIR == rawType) {
            null
        } else {
            rawType
        }
    }

    fun getFilePath(context: Context, self: Uri): String? {

        try {
            val authority = self.authority
            if (AUTHORITY_DOCUMENT_EXTERNAL_STORAGE == authority) {
                // Get type and path
                val docId = DocumentsContract.getDocumentId(self)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val path = split[1]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + path
                } else {
                    // Get the storage path
                    val cacheDirs = context.externalCacheDirs
                    var storageDir: String? = null
                    for (cacheDir in cacheDirs) {
                        val cachePath = cacheDir.path
                        val index = cachePath.indexOf(type)
                        if (index >= 0) {
                            storageDir = cachePath.substring(0, index + type.length)
                        }
                    }

                    return if (storageDir != null) {
                        "$storageDir/$path"
                    } else {
                        null
                    }
                }
            } else if (AUTHORITY_DOCUMENT_DOWNLOAD == authority) {
                val id = DocumentsContract.getDocumentId(self)

                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )

                return Contract.queryForString(context, contentUri, MediaStore.MediaColumns.DATA, null)
            } else if (AUTHORITY_DOCUMENT_MEDIA == authority) {
                // Get type and id
                val docId = DocumentsContract.getDocumentId(self)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val id = split[1]
                val baseUri = if ("image" == type) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
//                    Log.d(TAG, "Unknown type in $AUTHORITY_DOCUMENT_MEDIA: $type")
                    return null
                }

                val contentUri = ContentUris.withAppendedId(baseUri, id.toLong())

                // Requires android.permission.READ_EXTERNAL_STORAGE or return null
                return Contract.queryForString(context, contentUri, MediaStore.MediaColumns.DATA, null)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun isDirectory(context: Context, self: Uri): Boolean {
        return DocumentsContract.Document.MIME_TYPE_DIR == getRawType(context, self)
    }

    fun isFile(context: Context, self: Uri): Boolean {
        val type = getRawType(context, self)
        return !(DocumentsContract.Document.MIME_TYPE_DIR == type || TextUtils.isEmpty(type))
    }

    fun lastModified(context: Context, self: Uri): Long {
        return Contract.queryForLong(context, self, DocumentsContract.Document.COLUMN_LAST_MODIFIED, -1L)
    }

    fun length(context: Context, self: Uri): Long {
        return Contract.queryForLong(context, self, DocumentsContract.Document.COLUMN_SIZE, -1L)
    }

    fun canRead(context: Context, self: Uri): Boolean {
        // Ignore if grant doesn't allow read
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        // Ignore documents without MIME
        return !TextUtils.isEmpty(getRawType(context, self))
    }

    fun canWrite(context: Context, self: Uri): Boolean {
        // Ignore if grant doesn't allow write
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val type = getRawType(context, self)
        val flags: Int = Contract.queryForInt(context, self, DocumentsContract.Document.COLUMN_FLAGS, 0)

        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) {
            return false
        }

        // Deletable documents considered writable
        if ((flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
            return true
        }

        if (DocumentsContract.Document.MIME_TYPE_DIR == type
            && (flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0
        ) {
            // Directories that allow create considered writable
            return true
        } else if (!TextUtils.isEmpty(type)
            && (flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0
        ) {
            // Writable normal files considered writable
            return true
        }

        return false
    }

    fun delete(context: Context, self: Uri): Boolean {
        try {
            return DocumentsContract.deleteDocument(context.contentResolver, self)
        } catch (e: Exception) {
            // Maybe user ejects tf card
//            Log.e(TAG, "Failed to renameTo", e)
            return false
        }
    }

    fun exists(context: Context, self: Uri): Boolean {
        val resolver = context.contentResolver

        var c: Cursor? = null
        try {
            c = resolver.query(self, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
            return null != c && c.count > 0
        } catch (e: Exception) {
            // Log.w(TAG, "Failed query: " + e);
            return false
        } finally {
            c?.safeCloseNonRuntime()
        }
    }

}