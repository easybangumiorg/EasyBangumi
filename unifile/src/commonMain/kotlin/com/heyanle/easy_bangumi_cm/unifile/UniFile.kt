package com.heyanle.easy_bangumi_cm.unifile

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI


/**
 * Created by heyanlin on 2024/12/4.
 */

expect open class UniFileFactory() {

    fun fromFile(file: File): UniFile

    fun fromUri(uri: URI): UniFile?

}

interface UniFile {

    companion object: UniFileFactory() {

    }

    fun createFile(displayName: String): UniFile?

    fun createDirectory(displayName: String): UniFile?

    fun getUri(): String

    fun getName(): String

    fun getFilePath(): String

    fun getParentFile(): UniFile?

    fun isDirectory(): Boolean

    fun isFile(): Boolean

    fun lastModified(): Long

    fun lenght(): Long

    fun canRead(): Boolean

    fun canWrite(): Boolean

    fun delete(): Boolean

    fun exists(): Boolean

    fun listFiles(filter: ((UniFile, String) -> Boolean)? = null): Array<UniFile?>

    fun findFile(displayName: String): UniFile?

    fun renameTo(displayName: String): Boolean

    @Throws(IOException::class)
    fun openOutputStream(append: Boolean = false): OutputStream

    @Throws(IOException::class)
    fun openInputStream(): InputStream

    // Only for RawFile
    fun getUniRandomAccessFile(mode: String): UniRandomAccessFile?


}