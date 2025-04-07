package com.heyanle.lib.unifile

import kotlinx.io.IOException
import okio.Path
import okio.Sink
import okio.Source


/**
 * Created by heyanlin on 2024/12/4.
 */

object UniFileFactory {

    fun fromPath(path: Path): UniFile? {

    }
}



interface UniFile {

    fun next(displayName: String): UniFile?

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

    fun openSink(append: Boolean): Sink

    fun openSource(): Source

}