package org.easybangumi.next.shared.foundation.image

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import dev.icerock.moko.resources.AssetResource
import org.easybangumi.next.lib.utils.ResourceOr
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate
import org.jetbrains.compose.animatedimage.loadAnimatedImage
import org.jetbrains.compose.animatedimage.loadResourceAnimatedImage

/**
 * Created by heyanle on 2025/2/27.
 */

private sealed class AnimationDesktopLoadState {
    data object Loading : AnimationDesktopLoadState()
    data class Success(val value: AnimatedImage) : AnimationDesktopLoadState()
    data class Error(val exception: Exception) : AnimationDesktopLoadState()
}

@Composable
actual fun AnimationImage(
    model: ResourceOr,
    contentDescription: String?,
    modifier: Modifier,
    placeholder: Painter? ,
    error: Painter?,
    onLoading: (() -> Unit)?,
    onSuccess: (() -> Unit)?,
    onError: (() -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    clipToBounds: Boolean ,
){


    var state by remember {
        mutableStateOf<AnimationDesktopLoadState>(AnimationDesktopLoadState.Loading)
    }

    LaunchedEffect(model) {
        state = try {
            if (model is AssetResource) {
                AnimationDesktopLoadState.Success(loadResourceAnimatedImage(model.filePath))
            } else {
                AnimationDesktopLoadState.Success(loadAnimatedImage(model.toString()))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            AnimationDesktopLoadState.Error(e)
        }
    }


    LaunchedEffect(state) {
        when(state){
            AnimationDesktopLoadState.Loading -> {
                onLoading?.invoke()
            }
            is AnimationDesktopLoadState.Success -> {
                onSuccess?.invoke()
            }
            is AnimationDesktopLoadState.Error -> {
                onError?.invoke()
            }
        }
    }

    when(val sta = state) {
        AnimationDesktopLoadState.Loading -> {
            if(placeholder != null){

                Image(
                    modifier = Modifier.let {
                        if (clipToBounds) it.clipToBounds() else it
                    }.then(modifier),
                    painter = placeholder,
                    contentDescription = contentDescription,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter,
                )
            }
        }
        is AnimationDesktopLoadState.Success -> {

            Image(
                modifier = Modifier.let {
                    if (clipToBounds) it.clipToBounds() else it
                }.then(modifier),
                bitmap = sta.value.animate(),
                contentDescription = contentDescription,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter,
            )
        }
        is AnimationDesktopLoadState.Error -> {
            if(error != null){
                Image(
                    modifier = Modifier.let {
                        if (clipToBounds) it.clipToBounds() else it
                    }.then(modifier),
                    painter = error,
                    contentDescription = contentDescription,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter,
                )
            }
        }
    }


}