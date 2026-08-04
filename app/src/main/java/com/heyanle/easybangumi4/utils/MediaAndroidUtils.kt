package com.heyanle.easybangumi4.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2024/7/4.
 */
object MediaAndroidUtils {

    suspend fun saveImage(
        bitmap: Bitmap,
        displayName: String,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = APP.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/" +
                            "${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/image",
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values,
                ) ?: error("无法创建截图文件")
                try {
                    resolver.openOutputStream(uri)?.use { output ->
                        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                            "截图编码失败"
                        }
                    } ?: error("无法写入截图文件")
                    resolver.update(
                        uri,
                        ContentValues().apply {
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                        },
                        null,
                        null,
                    )
                    uri
                } catch (throwable: Throwable) {
                    resolver.delete(uri, null, null)
                    throw throwable
                }
            } else {
                val picturesRoot =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        ?: File(APP.getFilePath())
                val targetRoot = File(
                    picturesRoot,
                    "${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/image",
                ).apply { check(mkdirs() || isDirectory) { "无法创建截图目录" } }
                val target = File(targetRoot, displayName)
                target.outputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        "截图编码失败"
                    }
                }
                mediaScan(APP, target.absolutePath)
                Uri.fromFile(target)
            }
        }
    }

    suspend fun mediaScan(context: Context, path: String) {
        suspendCoroutine<Unit> {
            MediaScannerConnection.scanFile(
                context, arrayOf(path), null
            ) { p0, p1 -> it.resume(Unit) }
        }

    }

    fun mediaScan(context: Context, path: List<String>, mime: List<String>? = null) {
        MediaScannerConnection.scanFile(
            context, path.toTypedArray(), mime?.toTypedArray()
        ) { p0, p1 ->
            "mediaScan: $p0, $p1".logi("MediaAndroidUtils")
        }
    }

    suspend fun saveToDownload(inputStream: InputStream, type: String, displayName: String) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val picturesFile =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        ?: File(APP.getFilePath())
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val targetRoot = File(
                        picturesFile,
                        "${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/${type}"
                    )
                    val target = File(targetRoot, displayName)
                    inputStream.copyTo(target.outputStream())
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOWNLOADS}/${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/${type}"
                        )
                    }
                    APP.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?.let { uri ->
                            APP.contentResolver.openOutputStream(uri)
                        }?.use {
                            inputStream.copyTo(it)
                        }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

    }
    suspend fun saveToDownload(file: File, type: String, displayName: String = file.name) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val picturesFile =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        ?: File(APP.getFilePath())
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val targetRoot = File(
                        picturesFile,
                        "${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/${type}"
                    )
                    val target = File(targetRoot, file.name)
                    file.copyTo(target, true)
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOWNLOADS}/${if (BuildConfig.DEBUG) "EasyBangumi.debug" else "EasyBangumi"}/${type}"
                        )
                    }
                    APP.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?.let { uri ->
                            APP.contentResolver.openOutputStream(uri)
                        }?.use {
                            file.inputStream().copyTo(it)
                        }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

    }

}
