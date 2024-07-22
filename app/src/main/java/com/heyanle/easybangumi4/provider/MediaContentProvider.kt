package com.heyanle.easybangumi4.provider

import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.test.internal.util.LogUtil
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.App
import com.heyanle.easybangumi4.BuildConfig
import com.hippo.unifile.UniFile
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
class MediaContentProvider: FileProvider() {

    companion object {
        fun getProviderUriFromUri(uri: String) : Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return uri.toUri()
                val uniFile = UniFile.fromUri(APP, uri.toUri())
                val path = uniFile?.filePath
                val contentUri = FileProvider.getUriForFile(APP, "${BuildConfig.APPLICATION_ID}.provider", File(path))
                return contentUri

                Uri.Builder().scheme("content")
                    .encodedAuthority("${BuildConfig.APPLICATION_ID}.provider")
                    .appendPath("document")
                    .appendQueryParameter("tree_document", URLEncoder.encode(uri, "utf-8")).build()
            } else {
                uri.toUri()
            }

        }
    }


    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Log.i("MediaContentProvider", "openFile: $uri")
        val u = URLDecoder.decode(uri.getQueryParameter("tree_document")
            ?: return super.openFile(uri, mode), "utf-8").toUri()
        return context?.contentResolver?.openFileDescriptor(u.apply {
            Log.i("MediaContentProvider", "openFile: $this")
        }, "r")
    }
}