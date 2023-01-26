package com.heyanle.lib_anim.cycdm

// https://github.com/saschpe/Kase64


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

internal fun String.encodeInternal(encoding: Encoding): String {
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

internal fun String.decodeInternal(encoding: Encoding): Sequence<Int> {
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

internal fun ByteArray.asCharArray(): CharArray {
    val chars = CharArray(size)
    for (i in chars.indices) {
        chars[i] = get(i).toInt().toChar()
    }
    return chars
}

val String.base64Encoded: String
    get() = encodeInternal(Encoding.Standard)

/**
 * Encode a [ByteArray] to Base64 standard encoded [String].
 *
 * See [RFC 4648 §4](https://datatracker.ietf.org/doc/html/rfc4648#section-4)
 */
val ByteArray.base64Encoded: String
    get() = asCharArray().concatToString().base64Encoded

/**
 * Decode a Base64 standard encoded [String] to [String].
 *
 * See [RFC 4648 §4](https://datatracker.ietf.org/doc/html/rfc4648#section-4)
 */
val String.base64Decoded: String
    get() = decodeInternal(Encoding.Standard).map { it.toChar() }.joinToString("")
        .dropLast(count { it == '=' })

/**
 * Decode a Base64 standard encoded [String] to [ByteArray].
 *
 * See [RFC 4648 §4](https://datatracker.ietf.org/doc/html/rfc4648#section-4)
 */
val String.base64DecodedBytes: ByteArray
    get() = decodeInternal(Encoding.Standard).map { it.toByte() }.toList()
        .dropLast(count { it == '=' }).toByteArray()

/**
 * Decode a Base64 standard encoded [ByteArray] to [String].
 *
 * See [RFC 4648 §4](https://datatracker.ietf.org/doc/html/rfc4648#section-4)
 */
val ByteArray.base64Decoded: String
    get() = asCharArray().concatToString().base64Decoded


