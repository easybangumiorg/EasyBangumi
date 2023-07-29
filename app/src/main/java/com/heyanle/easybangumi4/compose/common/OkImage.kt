package com.heyanle.easybangumi4.compose.common

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    errorColor: Color? = MaterialTheme.colorScheme.error,
    errorRes: Int? = null,
    placeholderColor: Color? = MaterialTheme.colorScheme.secondaryContainer,
    placeholderRes: Int? = null,
    tint: Color? = null,
) {
    when (image) {
        is ImageVector -> {
            Image(
                imageVector = image,
                modifier = modifier,
                contentDescription = contentDescription,
                colorFilter = if(tint == null) null else ColorFilter.tint(tint)
            )
        }

        is Int -> {
            Image(
                painterResource(id = image),
                modifier = modifier,
                contentDescription = contentDescription,
                colorFilter = if(tint == null) null else ColorFilter.tint(tint)
            )
        }

        else -> {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(image)
                    .apply {
                        if(placeholderRes == null){
                            placeholderColor?.let {
                                placeholder(ColorDrawable(it.toArgb()))
                            }
                        }else{
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
                        if(errorRes == null){
                            errorColor?.let {
                                error(ColorDrawable(it.toArgb()))
                            }
                        }else{
                            error(errorRes)
                        }

                    }
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.then(modifier)
            )
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