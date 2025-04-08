package org.easybangumi.next.lib.unifile

import okio.Sink
import okio.Source
import okio.sink
import okio.source

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
class AUniFileWrapper(
    private val uniFile: AUniFile
): UniFile {

    override fun getType(): String {
        return UFD.TYPE_ANDROID_UNI
    }

    override fun getUri(): String {
        return uniFile.uri.toString()
    }

    override fun getName(): String {
        return uniFile.name ?: ""
    }

    override fun getFilePath(): String {
        return uniFile.filePath ?: ""
    }

    override fun getParentFile(): UniFile? {
        return uniFile.parentFile?.let {AUniFileWrapper(it)}
    }

    override fun isDirectory(): Boolean {
        return uniFile.isDirectory
    }

    override fun isFile(): Boolean {
        return uniFile.isFile
    }

    override fun lastModified(): Long {
        return uniFile.lastModified()
    }

    override fun lenght(): Long {
        return uniFile.length()
    }

    override fun exists(): Boolean {
        return uniFile.exists()
    }

    override fun child(displayName: String): UniFile? {
        return uniFile.findFile(displayName)?.let { AUniFileWrapper(it) }
    }

    override fun childIfExist(displayName: String): UniFile? {
        val child = uniFile.findFile(displayName)?.let { AUniFileWrapper(it) } ?: return null
        if (child.exists()) {
            return child
        }
        return null
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        return if (filter == null) {
            uniFile.listFiles()
        }else{
            uniFile.listFiles { au, n ->
                filter(AUniFileWrapper(au), n)
            }
        }?.map { it?.let { AUniFileWrapper(it) } }
            ?.toTypedArray() ?: emptyArray()
    }

    override fun canRead(): Boolean {
        return uniFile.canRead()
    }

    override fun canWrite(): Boolean {
        return uniFile.canWrite()
    }

    override fun delete(): Boolean {
        return uniFile.delete()
    }

    override fun createDirectory(displayName: String): UniFile? {
        return uniFile.createDirectory(displayName)?.let { AUniFileWrapper(it) }
    }

    override fun renameTo(displayName: String): Boolean {
        return uniFile.renameTo(displayName)
    }

    override fun openSink(append: Boolean): Sink {
        return uniFile.openOutputStream(append).sink()
    }

    override fun openSource(): Source {
        return uniFile.openInputStream().source()
    }
}