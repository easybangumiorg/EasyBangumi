package com.heyanle.easy_bangumi_cm.unifile.core

import android.content.ContentResolver
import android.content.res.AssetManager
import android.net.Uri
import com.heyanle.easy_bangumi_cm.unifile.*
import com.heyanle.easy_bangumi_cm.unifile.UniUtils.resolve
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by heyanlin on 2024/12/4.
 */
class AssetsFile(
    private val parent: IUniFile?,
    private val assetsManager: AssetManager,
    private val path: String,
): IUniFile, TypeIUniFile {

    override fun createFile(displayName: String): IUniFile? {
        val file = findFile(displayName)
        return if (file != null && file.isFile()) {
            file
        } else {
            null
        }
    }

    override fun createDirectory(displayName: String): IUniFile? {
        val file = findFile(displayName)
        return if (file != null && file.isDirectory()) {
            file
        } else {
            null
        }
    }

    override fun getUri(): String {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_FILE)
            .authority("")
            .path("android_asset/$path")
            .build()
            .toString()
    }

    override fun getName(): String {
        val index = path.lastIndexOf('/');
        if (index >= 0 && index < path.length - 1) {
            return path.substring(index + 1);
        } else {
            return path;
        }
    }

    override fun getFilePath(): String {
        return path
    }

    override fun getParentFile(): IUniFile? {
        if (parent != null) {
            return parent
        }
        val index = path.lastIndexOf('/');
        if (index >= 0) {
            return AssetsFile(null, assetsManager, path.substring(0, index))
        } else {
            return null
        }
    }

    override fun isDirectory(): Boolean {
        try {
            val files: Array<String> = assetsManager.list(path) ?: emptyArray()
            return files.isNotEmpty()
        } catch (e: IOException) {
            return false
        }
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
        return -1
    }

    override fun lenght(): Long {
        return -1
    }

    override fun canRead(): Boolean {
        return isFile()
    }

    override fun canWrite(): Boolean {
        return false
    }

    override fun delete(): Boolean {
        return false
    }

    override fun exists(): Boolean {
        return isDirectory() || isFile();
    }

    override fun listFiles(filter: ((IUniFile, String) -> Boolean)?): Array<IUniFile?> {
        try {
            val files: Array<String> = assetsManager.list(path) ?: emptyArray()
            if (files.isEmpty()) {
                return emptyArray()
            }

            val length = files.size
            val results = arrayOfNulls<IUniFile>(length)
            for (i in 0..<length) {
                val name = files[i]
                if (filter != null && !filter(this, name)) {
                    continue
                }
                results[i] = AssetsFile(this, assetsManager, UniUtils.resolve(path, name))
            }
            return results
        } catch (e: IOException) {
            return emptyArray()
        }
    }

    override fun findFile(displayName: String): IUniFile? {
        if (displayName.isEmpty()) {
            return null
        }

        try {
            val files: Array<String> = assetsManager.list(path) ?: emptyArray()
            if (files.isEmpty()) {
                return null
            }

            for (f in files) {
                if (displayName == f) {
                    return AssetsFile(this, assetsManager, resolve(path, displayName))
                }
            }

            return null
        } catch (e: IOException) {
            return null
        }
    }

    override fun renameTo(displayName: String): Boolean {
        return false
    }

    override fun openOutputStream(append: Boolean): OutputStream {
        throw IOException("Not support OutputStream for asset file.");
    }

    override fun openInputStream(): InputStream {
        return assetsManager.open(path);
    }

    override fun getUniRandomAccessFile(mode: String): UniRandomAccessFile? {
        // Not support RandomAccessFile for asset file.
        val afd = assetsManager.openFd(path)
        return RawRandomAccessFile(FDRandomAccessFile.from(afd, mode) ?: return null)
    }
}