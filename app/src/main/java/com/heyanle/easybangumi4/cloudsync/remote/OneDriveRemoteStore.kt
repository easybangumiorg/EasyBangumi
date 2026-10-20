package com.heyanle.easybangumi4.cloudsync.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

interface OneDriveTokenProvider {
    suspend fun accessToken(): String
}

class OneDriveRemoteStore(
    private val client: OkHttpClient,
    private val tokenProvider: OneDriveTokenProvider,
) : CloudSyncRemoteStore {

    override suspend fun probe() = withContext(Dispatchers.IO) {
        execute(authorizedRequest(APP_ROOT_URL).get().build()).use { response ->
            if (!response.isSuccessful) throw responseError("OneDrive 连接失败", response.code)
        }
    }

    override suspend fun read(): RemoteCloudFile? = withContext(Dispatchers.IO) {
        val metadata = execute(authorizedRequest(FILE_METADATA_URL).get().build()).use { response ->
            when {
                response.code == 404 -> return@withContext null
                !response.isSuccessful -> throw responseError("OneDrive 元数据读取失败", response.code)
                else -> JSONObject(response.body?.string().orEmpty()).optString("eTag")
                    .ifBlank {
                        throw CloudSyncRemoteException(
                            "OneDrive 未返回文件版本",
                            CloudSyncRemoteException.Kind.INVALID_RESPONSE,
                        )
                    }
            }
        }
        execute(authorizedRequest(FILE_CONTENT_URL).get().build()).use { response ->
            if (!response.isSuccessful) throw responseError("OneDrive 下载失败", response.code)
            RemoteCloudFile(
                bytes = response.body?.bytes()
                    ?: throw CloudSyncRemoteException(
                        "OneDrive 返回了空响应",
                        CloudSyncRemoteException.Kind.INVALID_RESPONSE,
                    ),
                version = metadata,
            )
        }
    }

    override suspend fun write(
        bytes: ByteArray,
        expectedVersion: String?,
    ): RemoteWriteResult = withContext(Dispatchers.IO) {
        val request = authorizedRequest(FILE_CONTENT_URL)
            .apply {
                if (expectedVersion == null) header("If-None-Match", "*")
                else header("If-Match", expectedVersion)
            }
            .put(bytes.toRequestBody(BINARY_MEDIA_TYPE))
            .build()
        execute(request).use { response ->
            when {
                response.code == 409 || response.code == 412 -> RemoteWriteResult.Conflict
                !response.isSuccessful -> throw responseError("OneDrive 上传失败", response.code)
                else -> {
                    val body = response.body?.string().orEmpty()
                    val version = runCatching { JSONObject(body).optString("eTag") }
                        .getOrDefault("")
                        .ifBlank { null }
                    RemoteWriteResult.Success(version)
                }
            }
        }
    }

    private suspend fun authorizedRequest(url: String): Request.Builder {
        val token = tokenProvider.accessToken()
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
    }

    private fun execute(request: Request) = try {
        client.newCall(request).execute()
    } catch (exception: IOException) {
        throw CloudSyncRemoteException(
            "无法连接 OneDrive：${exception.message.orEmpty()}",
            CloudSyncRemoteException.Kind.NETWORK,
            exception,
        )
    }

    private fun responseError(prefix: String, code: Int): CloudSyncRemoteException {
        val kind = if (code == 401 || code == 403) {
            CloudSyncRemoteException.Kind.AUTH_REQUIRED
        } else {
            CloudSyncRemoteException.Kind.SERVER
        }
        return CloudSyncRemoteException("$prefix：HTTP $code", kind)
    }

    private companion object {
        const val GRAPH_ROOT = "https://graph.microsoft.com/v1.0/me/drive/special/approot"
        const val APP_ROOT_URL = GRAPH_ROOT
        const val FILE_METADATA_URL = "$GRAPH_ROOT:/easybangumi-sync-v1.json.gz"
        const val FILE_CONTENT_URL = "$FILE_METADATA_URL:/content"
        val BINARY_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
