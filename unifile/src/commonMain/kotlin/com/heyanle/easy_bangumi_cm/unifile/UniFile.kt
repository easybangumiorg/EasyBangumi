package com.heyanle.easy_bangumi_cm.unifile

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI


/**
 * Created by heyanlin on 2024/12/4.
 */

expect object UniFile {

    fun fromFile(file: File): IUniFile

    fun fromUri(uri: URI): IUniFile?

}

interface IUniFile {

    fun createFile(displayName: String): IUniFile?

    fun createDirectory(displayName: String): IUniFile?

    fun getUri(): String

    fun getName(): String

    fun getFilePath(): String

    fun getParentFile(): IUniFile?

    fun isDirectory(): Boolean

    fun isFile(): Boolean

    fun lastModified(): Long

    fun lenght(): Long

    fun canRead(): Boolean

    fun canWrite(): Boolean

    fun delete(): Boolean

    fun exists(): Boolean

    fun listFiles(filter: ((IUniFile, String) -> Boolean)? = null): Array<IUniFile?>

    fun findFile(displayName: String): IUniFile?

    fun renameTo(displayName: String): Boolean

    @Throws(IOException::class)
    fun openOutputStream(append: Boolean = false): OutputStream

    @Throws(IOException::class)
    fun openInputStream(): InputStream

    // Only for RawFile
    fun getUniRandomAccessFile(mode: String): UniRandomAccessFile?


}