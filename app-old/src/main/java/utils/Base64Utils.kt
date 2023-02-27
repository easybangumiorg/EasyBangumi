package com.heyanle.lib_anim.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Base64Utils {
    internal sealed interface Encoding {
        val alphabet: String
        val requiresPadding: Boolean

        object Standard : Encoding {
            override val alphabet: String =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            override val requiresPadding: Boolean = true
        }

        object UrlSafe : Encoding {
            override val alphabet: String =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            override val requiresPadding: Boolean = false // Padding is optional
        }
    }

    private fun String.encodeInternal(encoding: Encoding): String {
        val padLength = when (length % 3) {
            1 -> 2
            2 -> 1
            else -> 0
        }
        val raw = this + 0.toChar().toString().repeat(maxOf(0, padLength))
        val encoded = raw.chunkedSequence(3) {
            Triple(it[0].code, it[1].code, it[2].code)
        }.map { (first, second, third) ->
            (0xFF.and(first) shl 16) + (0xFF.and(second) shl 8) + 0xFF.and(third)
        }.map { n ->
            sequenceOf((n shr 18) and 0x3F, (n shr 12) and 0x3F, (n shr 6) and 0x3F, n and 0x3F)
        }.flatten()
            .map { encoding.alphabet[it] }
            .joinToString("")
            .dropLast(padLength)
        return when (encoding.requiresPadding) {
            true -> encoded.padEnd(encoded.length + padLength, '=')
            else -> encoded
        }
    }

    private fun String.decodeInternal(encoding: Encoding): Sequence<Int> {
        val padLength = when (length % 4) {
            1 -> 3
            2 -> 2
            3 -> 1
            else -> 0
        }
        return padEnd(length + padLength, '=')
            .replace("=", "A")
            .chunkedSequence(4) {
                (encoding.alphabet.indexOf(it[0]) shl 18) + (encoding.alphabet.indexOf(it[1]) shl 12) +
                        (encoding.alphabet.indexOf(it[2]) shl 6) + encoding.alphabet.indexOf(it[3])
            }
            .map { sequenceOf(0xFF.and(it shr 16), 0xFF.and(it shr 8), 0xFF.and(it)) }
            .flatten()
    }

    private fun ByteArray.asCharArray(): CharArray {
        val chars = CharArray(size)
        for (i in chars.indices) {
            chars[i] = get(i).toInt().toChar()
        }
        return chars
    }

    private val String.base64Encoded: String
        get() = encodeInternal(Encoding.Standard)

    private val String.base64Decoded: String
        get() = decodeInternal(Encoding.Standard).map { it.toChar() }.joinToString("")
            .dropLast(count { it == '=' })

    private val String.base64UrlEncoded: String
        get() = encodeInternal(Encoding.UrlSafe)

    private val String.base64UrlDecoded: String
        get() {
            val ret = decodeInternal(Encoding.UrlSafe).map { it.toChar() }
            val foo = ret.joinToString("")
            val bar = foo.dropLast(count { it == '=' })
            return bar.filterNot { it.code == 0 }
        }

    fun encodeUrl(string: String): String {
        return string.base64UrlEncoded
    }

    fun decodeUrl(string: String): String {
        return string.base64UrlDecoded
    }

    fun encode(string: String): String {
        return string.base64Encoded
    }

    fun decode(string: String): String {
        return string.base64Decoded
    }

    fun getMD5(text: String): String {
        try {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            val digest: ByteArray = instance.digest(text.toByteArray())
            var sb: StringBuffer = StringBuffer()
            for (b in digest) {
                var i: Int = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    hexString = "0" + hexString
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}