package com.heyanle.easybangumi4.cloudsync.remote

data class RemoteCloudFile(
    val bytes: ByteArray,
    val version: String?,
)

sealed interface RemoteWriteResult {
    data class Success(val version: String?) : RemoteWriteResult
    data object Conflict : RemoteWriteResult
}

interface CloudSyncRemoteStore {
    suspend fun probe()
    suspend fun read(): RemoteCloudFile?
    suspend fun write(bytes: ByteArray, expectedVersion: String?): RemoteWriteResult
}

class CloudSyncRemoteException(
    message: String,
    val kind: Kind,
    cause: Throwable? = null,
) : Exception(message, cause) {
    enum class Kind {
        NOT_CONFIGURED,
        AUTH_REQUIRED,
        NETWORK,
        SERVER,
        INVALID_RESPONSE,
    }
}
