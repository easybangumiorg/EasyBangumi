package com.heyanle.easy_bangumi_cm.common.compose.image

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder

@Composable
actual fun createImageLoader(): ImageLoader {
    return ImageLoader.Builder(LocalContext.current)
        .apply {
            diskCachePolicy(CachePolicy.ENABLED)
            memoryCachePolicy(CachePolicy.ENABLED)
            memoryCache {
                MemoryCache.Builder().apply {
                    maxSizeBytes(10 * 1024 * 1024)
                }.build()
            }
            networkCachePolicy(CachePolicy.ENABLED)
            components {
                add(SvgDecoder.Factory())
                add(
                    KtorNetworkFetcherFactory()
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
        }.build()
}