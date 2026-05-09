package org.easybangumi.next.shared.download.action.m3u8

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.readBytes
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * M3U8 AES-128-CBC 解密器
 * 纯 Kotlin/Java 实现，跨平台
 */
object M3u8Decryptor {

    /**
     * AES-128-CBC 解密
     */
    fun decryptAes128Cbc(
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray,
    ): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(data)
    }

    /**
     * 从 URL 获取密钥
     */
    suspend fun fetchKey(
        client: HttpClient,
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): ByteArray {
        return client.prepareGet(url) {
            headers.forEach { (k, v) -> header(k, v) }
        }.execute { response ->
            response.readBytes()
        }
    }

    /**
     * 生成默认 IV（使用分片序号）
     */
    fun generateIV(segmentIndex: Int): ByteArray {
        val iv = ByteArray(16)
        // 将 segmentIndex 写入 IV 的最后 4 个字节（大端序）
        iv[12] = (segmentIndex shr 24).toByte()
        iv[13] = (segmentIndex shr 16).toByte()
        iv[14] = (segmentIndex shr 8).toByte()
        iv[15] = segmentIndex.toByte()
        return iv
    }
}
