package com.heyanle.easybangumi4.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

/**
 * Created by heyanle on 2024/6/1.
 * https://github.com/heyanLE
 */
object FileUtils {

    const val TAG: String = "FileUtils"
    const val PRIMARY_VOLUME_NAME: String = "primary"

    fun traverseFolder(folder: File?, res: ArrayList<Pair<String, Long>>){
        traverseFolder(folder, arrayListOf(), res)

    }

    private fun traverseFolder(folder: File?, path: ArrayList<String>, res: ArrayList<Pair<String, Long>>){
        if (folder == null || !folder.exists()){
            return
        }
        if (folder.isFile){
            res.add(path.joinToString(File.separator) + File.separator + folder.name to folder.length())
            return
        }
        path.add(folder.name)
        folder.listFiles()?.forEach {
            traverseFolder(it, path, res)
        }
        path.removeLast()
    }

    fun getFullPathFromTreeUri(treeUri: Uri, con: Context): String? {
        if (treeUri == null) {
            return null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (isDownloadsDocument(treeUri)) {
                val docId = DocumentsContract.getDocumentId(treeUri)
                val extPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                if (docId == "downloads") {
                    return extPath
                } else if (docId.matches("^ms[df]\\:.*".toRegex())) {
                    val fileName: String? = FileUtils.getFileName(treeUri, con)
                    return "$extPath/$fileName"
                } else if (docId.startsWith("raw:")) {
                    val rawPath = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                    return rawPath
                }
                return null
            }
        }

        var volumePath: String =
            getVolumePath(getVolumeIdFromTreeUri(treeUri) , con)
                ?: return File.separator

        if (volumePath.endsWith(File.separator)) volumePath =
            volumePath.substring(0, volumePath.length - 1)

        var documentPath: String = getDocumentPathFromTreeUri(treeUri)

        if (documentPath.endsWith(File.separator)) documentPath =
            documentPath.substring(0, documentPath.length - 1)

        return if (documentPath.isNotEmpty()) {
            if (documentPath.startsWith(File.separator)) {
                volumePath + documentPath
            } else {
                volumePath + File.separator + documentPath
            }
        } else {
            volumePath
        }
    }

    fun getFileName(uri: Uri, context: Context): String? {
        var result: String? = null

        try {
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e(
                FileUtils.TAG,
                "Failed to handle file name: $ex"
            )
        }

        return result
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun getVolumePath(volumeId: String?, context: Context): String? {
        try {
            val mStorageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
            val getUuid = storageVolumeClazz.getMethod("getUuid")
            val isPrimary = storageVolumeClazz.getMethod("isPrimary")
            val result = getVolumeList.invoke(mStorageManager) ?: return null

            val length = java.lang.reflect.Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = java.lang.reflect.Array.get(result, i)
                val uuid = getUuid.invoke(storageVolumeElement) as String
                val primary = isPrimary.invoke(storageVolumeElement) as Boolean

                // primary volume?
                if (primary != null && FileUtils.PRIMARY_VOLUME_NAME == volumeId) {
                    return FileUtils.getDirectoryPath(storageVolumeClazz, storageVolumeElement)
                }

                // other volumes?
                if (uuid != null && uuid == volumeId) {
                    return FileUtils.getDirectoryPath(storageVolumeClazz, storageVolumeElement)
                }
            }
            // not found.
            return null
        } catch (ex: Exception) {
            return null
        }
    }

    fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        return if (split.isNotEmpty()) split[0]
        else null
    }



    fun getDocumentPathFromTreeUri(treeUri: Uri): String {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split: Array<String?> = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        return if ((split.size >= 2) && (split[1] != null)) split[1]!!
        else File.separator
    }

    private fun getDirectoryPath(storageVolumeClazz: Class<*>, storageVolumeElement: Any?): String? {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                val getPath = storageVolumeClazz.getMethod("getPath")
                return getPath.invoke(storageVolumeElement) as String
            }

            val getDirectory = storageVolumeClazz.getMethod("getDirectory")
            val f = getDirectory.invoke(storageVolumeElement) as File
            return f.path
        } catch (ex: java.lang.Exception) {
            return null
        }
    }
}