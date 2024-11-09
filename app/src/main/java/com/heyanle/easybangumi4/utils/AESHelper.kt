package com.heyanle.easybangumi4.utils

import androidx.annotation.WorkerThread
import java.io.File
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * Created by heyanle on 2024/10/13
 * https://github.com/heyanLE
 */
@WorkerThread
fun File.aesEncryptTo(file: File, key: String, chunkSize: Int) {
    try {
        if (!exists() || isDirectory || !canRead()) {
            return
        }
        val parent = file.parent ?: return
        if (!File(parent).exists()) {
            File(parent).mkdirs()
        }
        file.delete()
        file.createNewFile()
        if (!file.canWrite()) {
            return
        }
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        inputStream().buffered().use { input ->
            file.outputStream().buffered().use { output ->
                CipherOutputStream(output, cipher).use { cos ->
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@WorkerThread
fun File.aesDecryptTo(file: File, key: String, chunkSize: Int) {
    try {
        if (!exists() || isDirectory || !canRead()) {
            return
        }
        val parent = file.parent ?: return
        if (!File(parent).exists()) {
            File(parent).mkdirs()
        }
        file.delete()
        file.createNewFile()
        if (!file.canWrite()) {
            return
        }
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        inputStream().buffered().use { input ->
            file.outputStream().buffered().use { output ->
                CipherInputStream(input, cipher).use { cis ->
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}