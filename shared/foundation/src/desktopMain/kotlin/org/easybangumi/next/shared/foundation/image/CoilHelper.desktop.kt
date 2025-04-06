package org.easybangumi.next.shared.foundation.image

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.InternalCoilApi
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
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
    return ImageLoader.Builder(PlatformContext.INSTANCE).apply {
        diskCachePolicy(CachePolicy.ENABLED)
        memoryCachePolicy(CachePolicy.ENABLED)
        memoryCache {
            MemoryCache.Builder().apply {
                maxSizeBytes(10 * 1024 * 1024)
            }.build()
        }
        networkCachePolicy(CachePolicy.ENABLED)
        components {
            add(MokoAssetResourceFetcher.AssetFactory())
            add(MokoImageResourceFetcher.ImageFactory())
            add(KtorNetworkFetcherFactory())
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
        val inputStream =  checkNotNull(model.resourcesClassLoader.getResourceAsStream(model.filePath)) { "Asset not found: ${model.filePath}" }
        return SourceFetchResult(
            source = ImageSource(inputStream.source().buffer(), options.fileSystem),
            mimeType = MimeTypeMap.getMimeTypeFromExtension(model.originalPath),
            dataSource = DataSource.DISK,
        )
    }

    class AssetFactory: Fetcher.Factory<AssetResource> {
        override fun create(data: AssetResource, options: Options, imageLoader: ImageLoader): Fetcher {
            return MokoAssetResourceFetcher(data, options)
        }
    }

}

private class MokoImageResourceFetcher(
    private val model: ImageResource,
    private val options: Options,
): Fetcher {

    @OptIn(InternalCoilApi::class)
    override suspend fun fetch(): FetchResult {
        val inputStream =  checkNotNull(model.resourcesClassLoader.getResourceAsStream(model.filePath)) { "Asset not found: ${model.filePath}" }
        return SourceFetchResult(
            source = ImageSource(inputStream.source().buffer(), options.fileSystem),
            mimeType = MimeTypeMap.getMimeTypeFromExtension(model.filePath),
            dataSource = DataSource.DISK,
        )
    }

    class ImageFactory: Fetcher.Factory<ImageResource> {
        override fun create(data: ImageResource, options: Options, imageLoader: ImageLoader): Fetcher {
            return MokoImageResourceFetcher(data, options)
        }
    }

}