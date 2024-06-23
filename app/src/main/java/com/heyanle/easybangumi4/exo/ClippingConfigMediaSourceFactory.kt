package com.heyanle.easybangumi4.exo

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.ClippingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * Created by heyanle on 2024/6/23.
 * https://github.com/heyanLE
 */
class ClippingConfigMediaSourceFactory(
    private val inner: MediaSource.Factory,
    private val clippingConfiguration: ClippingConfiguration
): MediaSource.Factory by inner {

    @UnstableApi
    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        return ClippingMediaSource(
            inner.createMediaSource(mediaItem),
            clippingConfiguration.startPositionUs,
            clippingConfiguration.endPositionUs,
            clippingConfiguration.startsAtKeyFrame,
            clippingConfiguration.relativeToLiveWindow,
            clippingConfiguration.relativeToDefaultPosition,
        )
    }
}