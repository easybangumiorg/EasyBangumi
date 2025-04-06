package com.heyanle.easy_bangumi_cm.common.compose.image

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.annotation.InternalCoilApi
import coil3.decode.AssetMetadata
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.Options
import coil3.svg.SvgDecoder
import coil3.util.MimeTypeMap
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.ImageResource
import okio.buffer
import okio.source

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
                add(KtorNetworkFetcherFactory())
                add(ImageResourceMapper())
                add(MokoAssetResourceFetcher.AssetFactory())

                add(SvgDecoder.Factory())


            }
        }.build()
}


private class MokoAssetResourceFetcher(
    private val model: AssetResource,
    private val options: Options,
): Fetcher {

    @OptIn(InternalCoilApi::class)
    override suspend fun fetch(): FetchResult {
        val inputStream =  checkNotNull(model.getInputStream(options.context)) { "Asset not found: ${model.path}" }
        return SourceFetchResult(
            source = ImageSource(inputStream.source().buffer(), options.fileSystem, AssetMetadata(model.path)),
            mimeType = MimeTypeMap.getMimeTypeFromExtension(model.path),
            dataSource = DataSource.DISK,
        )
    }

    class AssetFactory: Fetcher.Factory<AssetResource> {
        override fun create(data: AssetResource, options: Options, imageLoader: ImageLoader): Fetcher {
            data.originalPath
            imageLoader.newBuilder()
            return MokoAssetResourceFetcher(data, options)
        }
    }
}

class ImageResourceMapper: Mapper<ImageResource, Int> {
    override fun map(data: ImageResource, options: Options): Int {
        return data.drawableResId
    }
}
