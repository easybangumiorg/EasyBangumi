package com.heyanle.easy_bangumi_cm.unifile

import android.content.res.AssetFileDescriptor
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * 支持 pfd 和 afd 的 RandomAccessFile
 * 通过反射替换 fd
 * Created by heyanlin on 2024/12/4.
 */
class FDRandomAccessFile private constructor(
    mode: String
    // 随机打开一个
) : RandomAccessFile("/dev/random", mode) {

    companion object {

        // 反射
        val FIELD_FD: Field? by lazy {
            try {
                val fd = RandomAccessFile::class.java.getDeclaredField("fd")
                fd.isAccessible = true
                return@lazy fd
            } catch (e: Throwable) {
                e.printStackTrace()
                return@lazy null
            }
        }


        val METHOD_CLOSE: Method? by lazy {
            try {
                val clazz = Class.forName("libcore.io.IoUtils")
                return@lazy clazz.getMethod("close", FileDescriptor::class.java)
            } catch (e: Throwable) {
                return@lazy null
            }
        }

        val WORK: Boolean by lazy {
            FIELD_FD != null && METHOD_CLOSE != null
        }


        fun from(afd: AssetFileDescriptor, mode: String): FDRandomAccessFile? {
            if (!WORK) {
                Log.e("FDRandomAccessFile", "Not work")
                return null
            }
            try {
                val fd = afd.fileDescriptor
                val source = from(fd, mode)
                source?.needClose = afd
                return source
            } catch (e: Throwable) {
                e.printStackTrace()
                afd.safeClose()
                return null
            }
        }

        fun from(pfd: ParcelFileDescriptor, mode: String): FDRandomAccessFile?  {
            if (!WORK) {
                Log.e("FDRandomAccessFile", "Not work")
                return null
            }
            try {
                val fd = pfd.fileDescriptor
                val source = from(fd, mode)
                source?.needClose = pfd
                return source
            } catch (e: Throwable) {
                e.printStackTrace()
                pfd.safeClose()
                return null
            }
        }

        fun from(fd: FileDescriptor, mode: String): FDRandomAccessFile?  {
            if (!WORK) {
                Log.e("FDRandomAccessFile", "Not work")
                return null
            }
            val source = try {
                FDRandomAccessFile(mode)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return null
            }

            try {
                val oldFd = FIELD_FD?.get(source) as? FileDescriptor
                METHOD_CLOSE?.invoke(null, oldFd)
                FIELD_FD?.set(source, fd)
                return source
            } catch (e: Throwable) {
                e.printStackTrace()
                source.safeClose()
                return null
            }
        }
    }

    private var needClose: Closeable? = null

    override fun close() {
        needClose?.safeClose()
        needClose = null
        super.close()
    }



}