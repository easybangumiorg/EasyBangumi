package com.heyanle.lib.unifile.core

import com.heyanle.lib.unifile.UniFile
import com.heyanle.lib.unifile.UniRandomAccessFile
import okio.*

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
class OkioUniFile(
    private val path: Path,
): UniFile {

    override fun createFile(displayName: String): UniFile? {

    }

    override fun createDirectory(displayName: String): UniFile? {
        TODO("Not yet implemented")
    }

    override fun getUri(): String {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getFilePath(): String {
        TODO("Not yet implemented")
    }

    override fun getParentFile(): UniFile? {
        TODO("Not yet implemented")
    }

    override fun isDirectory(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFile(): Boolean {
        TODO("Not yet implemented")
    }

    override fun lastModified(): Long {
        TODO("Not yet implemented")
    }

    override fun lenght(): Long {
        TODO("Not yet implemented")
    }

    override fun canRead(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canWrite(): Boolean {
        TODO("Not yet implemented")
    }

    override fun delete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        TODO("Not yet implemented")
    }

    override fun findFile(displayName: String): UniFile? {
        TODO("Not yet implemented")
    }

    override fun renameTo(displayName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun openSink(append: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun openSource(): Source {
        TODO("Not yet implemented")
    }

    override fun getUniRandomAccessFile(mode: String): UniRandomAccessFile? {
        TODO("Not yet implemented")
    }
}