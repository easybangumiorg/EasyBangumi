package org.easybangumi.next.lib.unifile

import okio.Sink
import okio.Source
import okio.sink
import okio.source
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import java.io.*

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class JvmUniFile(
    private val file: File
) : UniFile {

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


    override fun createDirectory(displayName: String): UniFile? {
        if (displayName.isEmpty()) {
            return null
        }

        val target: File = File(file, displayName)
        return if (target.isDirectory || target.mkdirs()) {
            JvmUniFile(target)
        } else {
            null
        }
    }

    override fun createDirectory(): Boolean {
        return file.mkdirs()
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

    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override fun isFile(): Boolean {
        return file.isFile
    }

    override fun lastModified(): Long {
        return file.lastModified()
    }

    override fun length(): Long {
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

    override fun getParentFile(): UniFile? {
        return file.parentFile?.let { JvmUniFile(it) }
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        val files = if (filter != null) file.listFiles(object: FilenameFilter {
            override fun accept(p0: File?, p1: String?): Boolean {
                return filter?.invoke(this@JvmUniFile, p1?:"")?:true
            }
        }) else file.listFiles()
        return files?.map {
            JvmUniFile(it)
        }?.toTypedArray()?: emptyArray()
    }

    override fun getType(): String {
        return UFD.TYPE_JVM
    }

    override fun child(displayName: String): UniFile? {
        val target = File(file, displayName)
        return JvmUniFile(target)
    }

    override fun resolve(relative: String): UniFile? {
        val target = File(file, relative)
        return JvmUniFile(target)
    }


    override fun openSink(append: Boolean): Sink {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            FileOutputStream(file, false).close()
        }
        return file.sink(append)
    }

    override fun openSource(): Source {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            FileOutputStream(file, false).close()
        }
        return file.source()
    }


    override fun renameTo(displayName: String): Boolean {
        val target = File(file.parent, displayName)
        return file.renameTo(target)
    }

}