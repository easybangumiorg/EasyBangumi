package com.heyanle.easybangumi4.ui.common

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

/**
 * Created by HeYanLe on 2023/1/10 16:42.
 * https://github.com/heyanLE
 */

@Composable
fun OkImage(
    modifier: Modifier = Modifier,
    image: Any?,
    contentDescription: String,
    isGif: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    crossFade: Boolean = true,
    errorColor: Color = MaterialTheme.colorScheme.error,
    placeholder: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .data(image)
            .placeholder(ColorDrawable(placeholder.toArgb()))
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
            .error(ColorDrawable(errorColor.toArgb()))
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = Modifier.then(modifier)
    )
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