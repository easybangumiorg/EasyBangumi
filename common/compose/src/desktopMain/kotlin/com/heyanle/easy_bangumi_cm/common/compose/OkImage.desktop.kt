package com.heyanle.easy_bangumi_cm.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.PlatformContext
import coil3.request.ImageRequest
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr

@Composable
internal actual fun OkImageImpl(
    modifier: Modifier,
    image: ResourceOr,
    contentDescription: String,
    isGif: Boolean,
    contentScale: ContentScale,
    crossFade: Boolean,
    errorColor: Color?,
    errorRes: ResourceOr?,
    placeholderColor: Color?,
    placeholderRes: ResourceOr?,
    tint: Color?,
    alpha: Float,
) {}

@Composable
internal actual fun getCoilImageRequestBuilder(): ImageRequest.Builder {
    return ImageRequest.Builder(PlatformContext.INSTANCE)
}