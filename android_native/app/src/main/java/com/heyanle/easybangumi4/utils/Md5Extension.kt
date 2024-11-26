package com.heyanle.easybangumi4.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.DigestInputStream

/**
 * Created by heyanlin on 2023/11/13.
 */
fun File.getFileMD5(): String {
    val digest = MessageDigest.getInstance("MD5")
    return kotlin.runCatching {
        FileInputStream(this).use {
            DigestInputStream(it, digest).use {
                while (it.read() != -1) { }
                val md5 = digest.digest()
                bytesToHex(md5)
            }
        }
    }.getOrElse {
        it.printStackTrace()
        ""
    }
}

fun String.getMD5(): String {
    val digest = MessageDigest.getInstance("MD5")
    return kotlin.runCatching {
        val md5 = digest.digest(this.toByteArray())
        bytesToHex(md5)
    }.getOrElse {
        it.printStackTrace()
        ""
    }
}

fun bytesToHex(bytes: ByteArray): String {
    val hexArray = "0123456789ABCDEF".toCharArray()
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}