package com.heyanle.lib.unifile

import java.io.IOException


/**
 * Created by heyanlin on 2024/12/4.
 */
interface UniRandomAccessFile {

    @Throws(IOException::class)
    fun close()

    @Throws(IOException::class)
    fun getFilePointer(): Long

    @Throws(IOException::class)
    fun seek(pos: Long)

    @Throws(IOException::class)
    fun skipBytes(n: Int): Int

    @Throws(IOException::class)
    fun length(): Long

    @Throws(IOException::class)
    fun setLength(newLength: Long)

    @Throws(IOException::class)
    fun read(b: ByteArray)

    @Throws(IOException::class)
    fun read(b: ByteArray, off: Int, len: Int)

    @Throws(IOException::class)
    fun write(b: ByteArray)

    @Throws(IOException::class)
    fun write(b: ByteArray, off: Int, len: Int)
}