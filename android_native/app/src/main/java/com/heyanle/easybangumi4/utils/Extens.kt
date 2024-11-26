package com.heyanle.easybangumi4.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.core.content.FileProvider
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Created by HeYanLe on 2023/8/13 13:53.
 * https://github.com/heyanLE
 */
val Int.kb get() = this * 1024
val Int.mb get() = this * 1024 * 1024
val Long.mb get() = this * 1024 * 1024
val Int.gb get() = this.toLong() * 1024 * 1024 * 1024
val Double.gb get() = (this * 1024 * 1024 * 1024).toLong()

fun shareText(text: String, ctx: Context = APP) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.type = "text/plain"
    val chooserIntent = Intent.createChooser(intent, stringRes(R.string.choose_target_app))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(chooserIntent)
}

fun shareImage(file: File) {
    shareImage(
        FileProvider.getUriForFile(
            APP, "${APP.packageName}.fileProvider", file
        )
    )
}

fun shareImage(uri: Parcelable, ctx: Context = APP) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.type = "image/*"
    val chooserIntent = Intent.createChooser(intent, stringRes(R.string.choose_target_app))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(chooserIntent)
}

fun shareImageText(uri: Parcelable, text: String, ctx: Context = APP) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.type = "*/*"
    val chooserIntent = Intent.createChooser(intent, stringRes(R.string.choose_target_app))
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(chooserIntent)
}

suspend fun downloadImage(url: String): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            OkHttpClient().newCall(
                Request.Builder()
                    .url(url)
                    .build()
            ).execute().use {
                it.body?.byteStream()?.let {
                    BitmapFactory.decodeStream(it)
                }
            }
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

fun bufferImageCache(
    bitmap: Bitmap,
    name: String = "${System.currentTimeMillis()}.jpg",
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 95
): File {
    return File(File(APP.cacheDir, "share").alsoMkdirs(), name).also {
        it.outputStream().use {
            bitmap.compress(format, quality, it)
        }
    }
}

fun File.alsoMkdirs() = also {
    if (!it.exists()) it.mkdirs()
}

fun File.alsoParentMkdirs() = also {
    if (it.parentFile?.exists() != true) it.parentFile?.mkdirs()
}
