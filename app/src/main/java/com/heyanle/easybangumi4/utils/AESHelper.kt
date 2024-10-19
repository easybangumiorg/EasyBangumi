package com.heyanle.easybangumi4.utils

import androidx.annotation.WorkerThread
import java.io.File
import javax.crypto.Cipher
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
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        inputStream().buffered().use { input ->
            file.outputStream().buffered().use { output ->
                val buffer = ByteArray(chunkSize)
                var len = input.read(buffer)
                while (len != -1) {
                    output.write(cipher.doFinal(buffer, 0, len))
                    len = input.read(buffer)
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
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        inputStream().buffered().use { input ->
            file.outputStream().buffered().use { output ->
                val buffer = ByteArray(chunkSize)
                var len = input.read(buffer)
                while (len != -1) {
                    output.write(cipher.doFinal(buffer, 0, len))
                    len = input.read(buffer)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}