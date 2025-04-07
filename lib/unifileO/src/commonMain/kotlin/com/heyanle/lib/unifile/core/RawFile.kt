package com.heyanle.lib.unifile.core

import com.heyanle.lib.unifile.UniFile
import com.heyanle.lib.unifile.UniRandomAccessFile
import java.io.*


/**
 * Created by heyanlin on 2024/12/4.
 */
class RawFile(
    private val parent: UniFile?,
    private val file: File,
): UniFile {

    companion object {
        private fun deleteContents(dir: File): Boolean {
            val files = dir.listFiles()
            var success = true
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        success = success and deleteContents(file)
                    }
                    if (!file.delete()) {
                        success = false
                    }
                }
            }
            return success
        }
    }



    override fun createFile(displayName: String): UniFile? {
        if (displayName.isEmpty()) {
            return null
        }
        val target: File = File(file, displayName)
        if (target.exists()) {
            return if (target.isFile) {
                RawFile(this, target)
            } else {
                null
            }
        } else {
            try {
                FileOutputStream(target).use {
                    return RawFile(this, target)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun createDirectory(displayName: String): UniFile? {
        if (displayName.isEmpty()) {
            return null
        }

        val target: File = File(file, displayName)
        return if (target.isDirectory || target.mkdirs()) {
            RawFile(this, target)
        } else {
            null
        }
    }

    override fun getUri(): String {
        return file.toURI().toString()
    }

    override fun getName(): String {
        return file.name

    }

    override fun getFilePath(): String {
        return file.absolutePath
    }

    override fun getParentFile(): UniFile? {
        if (parent != null) {
            return parent
        }
        val fileParent = file.parentFile ?: return null
        return RawFile(null, fileParent)
    }

    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override fun isFile(): Boolean {
        return file.isFile
    }

    override fun lastModified(): Long {
        return file.lastModified()
    }

    override fun lenght(): Long {
        return file.length()
    }

    override fun canRead(): Boolean {
        return file.canRead()
    }

    override fun canWrite(): Boolean {
        return file.canWrite()
    }

    override fun delete(): Boolean {
        deleteContents(file)
        return file.delete()
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        val files = if (filter != null) file.listFiles(object: FilenameFilter {
            override fun accept(p0: File?, p1: String?): Boolean {
                return filter?.invoke(this@RawFile, p1?:"")?:true
            }
        }) else file.listFiles()
        return files?.map {
            RawFile(this, it)
        }?.toTypedArray()?: emptyArray()
    }

    override fun findFile(displayName: String): UniFile? {
        val target = File(file, displayName)
        return if (target.exists()) {
            RawFile(this, target)
        } else {
            null
        }
    }

    override fun renameTo(displayName: String): Boolean {
        val target = File(file.parent, displayName)
        return file.renameTo(target)
    }

    override fun openOutputStream(append: Boolean): OutputStream {
        return FileOutputStream(file, append)
    }

    override fun openInputStream(): InputStream {
        return FileInputStream(file)
    }

    override fun getUniRandomAccessFile(mode: String): UniRandomAccessFile {
        return RawRandomAccessFile(RandomAccessFile(file, mode))
    }
}