package com.heyanle.easybangumi4.cloudsync

import android.content.Context
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncDocument
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class CloudSyncLocalStore(
    context: Context,
    moshi: Moshi,
) {
    private val adapter = moshi.adapter(CloudSyncDocument::class.java)
    private val mutex = Mutex()
    private val folder = File(context.filesDir, "cloud_sync")
    private val stateFile = File(folder, "local_state_v1.json")
    private val tempFile = File(folder, "local_state_v1.json.tmp")

    suspend fun read(): CloudSyncDocument = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!stateFile.exists() && !tempFile.exists()) return@withLock CloudSyncDocument()
            readFile(stateFile) ?: readFile(tempFile)
            ?: throw IllegalStateException("本机云同步状态已损坏")
        }
    }

    suspend fun write(document: CloudSyncDocument) = withContext(Dispatchers.IO) {
        document.validate()
        mutex.withLock {
            folder.mkdirs()
            tempFile.writeText(adapter.toJson(document), Charsets.UTF_8)
            if (stateFile.exists() && !stateFile.delete()) {
                throw IllegalStateException("Unable to replace local cloud sync state")
            }
            if (!tempFile.renameTo(stateFile)) {
                stateFile.writeText(tempFile.readText(Charsets.UTF_8), Charsets.UTF_8)
                tempFile.delete()
            }
        }
    }

    private fun readFile(file: File): CloudSyncDocument? {
        if (!file.isFile || !file.canRead()) return null
        return runCatching {
            adapter.fromJson(file.readText(Charsets.UTF_8))?.also(CloudSyncDocument::validate)
        }.getOrNull()
    }
}
