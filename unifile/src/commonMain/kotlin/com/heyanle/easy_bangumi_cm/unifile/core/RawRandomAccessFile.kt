package com.heyanle.easy_bangumi_cm.unifile.core

import com.heyanle.easy_bangumi_cm.unifile.UniRandomAccessFile
import java.io.RandomAccessFile

/**
 * Created by heyanlin on 2024/12/4.
 */
class RawRandomAccessFile(
    private val file: RandomAccessFile
): UniRandomAccessFile {

    override fun close() {
        file.close()
    }

    override fun getFilePointer(): Long {
        return file.filePointer
    }

    override fun seek(pos: Long) {
        file.seek(pos)
    }

    override fun skipBytes(n: Int): Int {
        return file.skipBytes(n)
    }

    override fun length(): Long {
        return file.length()
    }

    override fun setLength(newLength: Long) {
        file.setLength(newLength)
    }

    override fun read(b: ByteArray) {
        file.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int) {
        file.read(b, off, len)
    }

    override fun write(b: ByteArray) {
        file.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        file.write(b, off, len)
    }
}