package com.heyanle.easybangumi4.ui.common

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import okhttp3.Headers

/**
 * Created by HeYanLe on 2023/1/10 16:42.
 * https://github.com/heyanLE
 */
/**
 * 解析封面 URL 中的请求头后缀约定：
 * `https://img.example.com/pic.webp@Referer=https://movie.douban.com/@User-Agent=Mozilla/5.0 ...`
 * 返回 (干净的图片 URL, 解析出的请求头)。
 * 兼容任意 @Key=Value 顺序与多个标记。
 */
fun parseCoverImage(image: Any?): Pair<Any?, Map<String, String>> {
    if (image !is String) return image to emptyMap()
    val url = image
    val markers = listOf("@Referer=", "@User-Agent=")
    val first = markers.mapNotNull { url.indexOf(it).takeIf { i -> i >= 0 } }.minOrNull()
        ?: return url to emptyMap()
    val clean = url.substring(0, first)
    val headers = mutableMapOf<String, String>()
    var pos = first
    while (pos < url.length) {
        val marker = markers.firstOrNull { url.startsWith(it, pos) } ?: break
        val key = when (marker) {
            "@Referer=" -> "Referer"
            else -> "User-Agent"
        }
        pos += marker.length
        val next = markers.mapNotNull { url.indexOf(it, pos).takeIf { i -> i >= 0 } }.minOrNull() ?: url.length
        headers[key] = url.substring(pos, next)
        pos = next
    }
    return clean to headers
}


@Composable
fun OkImage(
    modifier: Modifier = Modifier,
    image: Any?,
    contentDescription: String,
    isGif: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    crossFade: Boolean = true,
    errorColor: Color? = MaterialTheme.colorScheme.error,
    errorRes: Int? = null,
    placeholderColor: Color? = MaterialTheme.colorScheme.secondaryContainer,
    placeholderRes: Int? = null,
    tint: Color? = null,
    alpha: Float = 1f,
    headers: Map<String, String>? = null,
) {
    var need = true
    if (image == null || image == "" || (image is Int && image <= 0)) {
        need = false
        if (errorRes != null) {
            Image(
                modifier = modifier,
                contentScale = contentScale,
                painter = painterResource(id = errorRes),
                contentDescription = contentDescription,
                alpha = alpha
            )
        } else if (errorColor != null) {
            Box(modifier = modifier.background(errorColor).alpha(alpha))
        } else if (placeholderRes != null) {
            Image(
                modifier = modifier,
                contentScale = contentScale,
                painter = painterResource(id = placeholderRes),
                contentDescription = contentDescription,
                alpha = alpha
            )
        } else if (placeholderColor != null) {
            Box(modifier = modifier.background(placeholderColor).alpha(alpha))
        } else {
            need = true
        }
    }
    if (need) {
        when (image) {
            is ImageVector -> {
                Image(
                    imageVector = image,
                    modifier = modifier,
                    contentScale = contentScale,
                    contentDescription = contentDescription,
                    colorFilter = if (tint == null) null else ColorFilter.tint(tint),
                    alpha = alpha
                )
            }

            is Int -> {
                Image(
                    painterResource(id = image),
                    modifier = modifier,
                    contentScale = contentScale,
                    contentDescription = contentDescription,
                    colorFilter = if (tint == null) null else ColorFilter.tint(tint),
                    alpha = alpha
                )
            }

            else -> {
                val (cleanImage, urlHeaders) = parseCoverImage(image)
                val mergedHeaders = urlHeaders + (headers ?: emptyMap())
                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(cleanImage)
                        .apply {
                            if (mergedHeaders.isNotEmpty()) {
                                headers(
                                    Headers.Builder().apply {
                                        mergedHeaders.forEach { (k, v) -> add(k, v) }
                                    }.build()
                                )
                            }
                        }
                        .apply {
                            if (placeholderRes == null) {
                                placeholderColor?.let {
                                    placeholder(ColorDrawable(it.toArgb()))
                                }
                            } else {
                                placeholder(placeholderRes)
                            }

                        }
                        .crossfade(crossFade)
                        .apply {
                            if (isGif) {
                                decoderFactory(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                        ImageDecoderDecoder.Factory()
                                    else GifDecoder.Factory()
                                )
                            }
                        }
                        .apply {
                            if (errorRes == null) {
                                errorColor?.let {
                                    error(ColorDrawable(it.toArgb()))
                                }
                            } else {
                                error(errorRes)
                            }

                        }
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.then(modifier),
                    alpha = alpha
                )
            }
        }
    }


}

@Composable
fun LoadingImage(
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .decoderFactory(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    ImageDecoderDecoder.Factory()
                else GifDecoder.Factory()
            )
            .crossfade(true)
            .data(Uri.parse("file:///android_asset/loading_ryo.gif")).build(),
        contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.loading),
        modifier = Modifier
            .then(modifier)
    )
}