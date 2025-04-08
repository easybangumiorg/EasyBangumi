package org.easybangumi.next.lib.unifile

import okio.IOException
import okio.Path
import okio.Sink
import okio.Source
import org.easybangumi.next.lib.unifile.core.OkioUniFile

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


object UniFileFactory {

    // 为 ios 准备，没有测试
    fun formPath(path: Path): UniFile {
        return OkioUniFile(path)
    }


}

expect fun UniFileFactory.fromUFD(ufd: UFD): UniFile?

interface UniFile {

    fun getType(): String

    // getter
    fun getUri(): String

    fun getName(): String

    fun getFilePath(): String

    fun getParentFile(): UniFile?

    fun isDirectory(): Boolean

    fun isFile(): Boolean

    fun lastModified(): Long

    fun lenght(): Long

    fun exists(): Boolean

    // children
    fun child(displayName: String): UniFile?

    fun childIfExist(displayName: String): UniFile?

    fun listFiles(filter: ((UniFile, String) -> Boolean)? = null): Array<UniFile?>


    // io
    fun canRead(): Boolean

    fun canWrite(): Boolean

    fun delete(): Boolean

    fun createDirectory(displayName: String): UniFile?

    fun renameTo(displayName: String): Boolean

    @Throws(IOException::class)
    fun openSink(append: Boolean): Sink

    @Throws(IOException::class)
    fun openSource(): Source

}