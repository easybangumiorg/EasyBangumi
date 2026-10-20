package com.heyanle.easybangumi4.cloudsync.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

data class WebDavConfig(
    val directoryUrl: String,
    val username: String,
    val password: String,
)

class WebDavRemoteStore(
    private val client: OkHttpClient,
    private val config: WebDavConfig,
) : CloudSyncRemoteStore {

    private val directoryUrl = config.directoryUrl.trim().trimEnd('/').toHttpUrlOrNull()
        ?: throw CloudSyncRemoteException("WebDAV 地址无效", CloudSyncRemoteException.Kind.NOT_CONFIGURED)
    private val fileUrl = directoryUrl.newBuilder().addPathSegment(FILE_NAME).build()
    private val authorization = Credentials.basic(config.username, config.password, Charsets.UTF_8)

    override suspend fun probe() = withContext(Dispatchers.IO) {
        val body = PROPFIND_BODY.toRequestBody(XML_MEDIA_TYPE)
        val request = requestBuilder(directoryUrl.toString())
            .header("Depth", "0")
            .method("PROPFIND", body)
            .build()
        execute(request).use { response ->
            if (response.code == 401 || response.code == 403) throw authError()
            if (response.code !in 200..299 && response.code != 207) {
                throw serverError("WebDAV 连接测试失败：HTTP ${response.code}")
            }
        }
    }

    override suspend fun read(): RemoteCloudFile? = withContext(Dispatchers.IO) {
        val request = requestBuilder(fileUrl.toString()).get().build()
        execute(request).use { response ->
            when {
                response.code == 404 -> null
                response.code == 401 || response.code == 403 -> throw authError()
                !response.isSuccessful -> throw serverError("WebDAV 下载失败：HTTP ${response.code}")
                else -> {
                    val version = response.header("ETag")
                        ?: throw invalidResponse("WebDAV 服务未提供 ETag，无法安全处理多设备冲突")
                    RemoteCloudFile(
                        bytes = response.body?.bytes()
                            ?: throw invalidResponse("WebDAV 返回了空响应"),
                        version = version,
                    )
                }
            }
        }
    }

    override suspend fun write(
        bytes: ByteArray,
        expectedVersion: String?,
    ): RemoteWriteResult = withContext(Dispatchers.IO) {
        val request = requestBuilder(fileUrl.toString())
            .apply {
                if (expectedVersion == null) header("If-None-Match", "*")
                else header("If-Match", expectedVersion)
            }
            .put(bytes.toRequestBody(BINARY_MEDIA_TYPE))
            .build()
        execute(request).use { response ->
            when {
                response.code == 412 -> RemoteWriteResult.Conflict
                response.code == 401 || response.code == 403 -> throw authError()
                !response.isSuccessful -> throw serverError("WebDAV 上传失败：HTTP ${response.code}")
                else -> RemoteWriteResult.Success(response.header("ETag"))
            }
        }
    }

    private fun requestBuilder(url: String): Request.Builder = Request.Builder()
        .url(url)
        .header("Authorization", authorization)
        .header("Accept", "application/octet-stream")

    private fun execute(request: Request) = try {
        client.newCall(request).execute()
    } catch (exception: IOException) {
        throw CloudSyncRemoteException(
            "无法连接 WebDAV：${exception.message.orEmpty()}",
            CloudSyncRemoteException.Kind.NETWORK,
            exception,
        )
    }

    private fun authError() = CloudSyncRemoteException(
        "WebDAV 认证失败，请检查账号或应用密码",
        CloudSyncRemoteException.Kind.AUTH_REQUIRED,
    )

    private fun serverError(message: String) = CloudSyncRemoteException(
        message,
        CloudSyncRemoteException.Kind.SERVER,
    )

    private fun invalidResponse(message: String) = CloudSyncRemoteException(
        message,
        CloudSyncRemoteException.Kind.INVALID_RESPONSE,
    )

    private companion object {
        const val FILE_NAME = "easybangumi-sync-v1.json.gz"
        val BINARY_MEDIA_TYPE = "application/octet-stream".toMediaType()
        val XML_MEDIA_TYPE = "application/xml; charset=utf-8".toMediaType()
        const val PROPFIND_BODY = """<?xml version="1.0"?><propfind xmlns="DAV:"><prop><resourcetype/></prop></propfind>"""
    }
}
