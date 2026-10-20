package com.heyanle.easybangumi4.cloudsync.model

import com.squareup.moshi.Moshi
import okio.Buffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class CloudSyncCodec(
    moshi: Moshi,
) {
    private val adapter = moshi.adapter(CloudSyncDocument::class.java)

    fun encode(document: CloudSyncDocument): ByteArray {
        val clean = document.cleanForRemote().also(CloudSyncDocument::validate)
        val output = Buffer()
        GZIPOutputStream(output.outputStream()).bufferedWriter(Charsets.UTF_8).use {
            it.write(adapter.toJson(clean))
        }
        return output.readByteArray()
    }

    fun decode(bytes: ByteArray): CloudSyncDocument {
        require(bytes.size <= MAX_COMPRESSED_BYTES) { "Cloud sync file is too large" }
        val json = GZIPInputStream(bytes.inputStream()).bufferedReader(Charsets.UTF_8).use {
            val buffer = CharArray(8 * 1024)
            buildString {
                while (true) {
                    val count = it.read(buffer)
                    if (count < 0) break
                    require(length + count <= MAX_JSON_CHARS) { "Cloud sync content is too large" }
                    append(buffer, 0, count)
                }
            }
        }
        return requireNotNull(adapter.fromJson(json)) { "Cloud sync file is invalid" }
            .also(CloudSyncDocument::validate)
    }

    private companion object {
        const val MAX_COMPRESSED_BYTES = 32 * 1024 * 1024
        const val MAX_JSON_CHARS = 128 * 1024 * 1024
    }
}
