package com.heyanle.easybangumi4.download.utils

import org.apache.commons.lang3.StringUtils
import java.io.File
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by HeYanLe on 2023/9/17 16:00.
 * https://github.com/heyanLE
 */
object M3U8Utils {

    /**
     * 解密ts
     *
     * @param sSrc   ts文件字节数组
     * @param length
     * @param sKey   密钥
     * @return 解密后的字节数组
     */
    @Throws(java.lang.Exception::class)
    fun decrypt(
        sSrc: ByteArray,
        length: Int,
        sKey: ByteArray,
        iv: String,
        method: String
    ): ByteArray? {
        if (StringUtils.isNotEmpty(method) && !method.contains("AES")) return null
        // 判断Key是否为16位
        if (sKey.size != 16) {
            return null
        }
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec =
            SecretKeySpec(sKey, "AES")
        var ivByte: ByteArray
        ivByte =
            if (iv.startsWith("0x")) hexStringToByteArray(iv.substring(2)) else iv.toByteArray()
        if (ivByte.size != 16) ivByte = ByteArray(16)
        //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
        val paramSpec: AlgorithmParameterSpec = IvParameterSpec(ivByte)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec)
        return cipher.doFinal(sSrc, 0, length)
    }

    private fun hexStringToByteArray(si: String): ByteArray {
        var s = si
        var len = s.length
        if (len and 1 == 1) {
            s = "0$s"
            len++
        }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = (((s[i].digitToIntOrNull(16) ?: 0) shl 4) + (s[i + 1].digitToIntOrNull(16)
                ?: 0)).toByte()
            i += 2
        }
        return data
    }

    fun deleteM3U8WithTs(path: String){
        val file = File(path)
        if(!file.exists() || !file.canRead()){
            return
        }
        val it = file.readLines().iterator()
        while(it.hasNext()){
            val line = it.next()
            if (line.startsWith("#EXTINF")) {
                if(it.hasNext()){
                    val ts = it.next()
                    val tsFile = File(ts)
                    tsFile.delete()
                }
            }
        }
        file.delete()
    }


}