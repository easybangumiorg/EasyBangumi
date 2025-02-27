package com.heyanle.easy_bangumi_cm.common.compose.image

import android.graphics.ImageDecoder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.ImageResource

/**
 * Created by heyanlin on 2025/2/27.
 */
@Composable
actual fun AnimationImage(
    model: ResourceOr,
    contentDescription: String?,
    modifier: Modifier,
    placeholder: Painter?,
    error: Painter?,
    onLoading: (() -> Unit)?,
    onSuccess: (() -> Unit)?,
    onError: (() -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    clipToBounds: Boolean,
){

    coil3.compose.AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .apply {
                data(model)
                decoderFactory(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        AnimatedImageDecoder.Factory()
                    else GifDecoder.Factory()
                )
            }
            .build(),
        contentDescription = contentDescription,
        imageLoader = LocalImageLoader.current,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = error,
        onLoading = {onLoading?.invoke()},
        onSuccess = {onSuccess?.invoke()},
        onError = {onError?.invoke()},
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = FilterQuality.None,
        clipToBounds = clipToBounds,
    )


}