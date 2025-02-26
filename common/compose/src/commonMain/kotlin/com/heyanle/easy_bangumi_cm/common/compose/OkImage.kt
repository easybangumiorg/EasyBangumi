package com.heyanle.easy_bangumi_cm.common.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import coil3.ColorImage
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.desc.image.asImageDesc
import org.jetbrains.skia.Bitmap

/**
 * Created by heyanlin on 2025/2/25.
 */
@Composable
fun OkImage(
    modifier: Modifier = Modifier,
    image: ResourceOr,
    contentDescription: String,
    isGif: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    crossFade: Boolean = true,
    errorColor: Color? = MaterialTheme.colorScheme.error,
    errorRes: ResourceOr? = null,
    placeholderColor: Color? = MaterialTheme.colorScheme.secondaryContainer,
    placeholderRes: ResourceOr? = null,
    tint: Color? = null,
    alpha: Float = 1f,
) {
    if (isGif) {
        // TODO GIF
    } else {
        if (image is ImageResource) {
            ResourceImage(
                modifier = modifier,
                image = image,
                color = null,
                contentScale = contentScale,
                contentDescription = contentDescription,
                tint = tint,
                alpha = alpha
            )
        }  else if (image is String) {
           // TODO
        } else  {
            // Error
            ResourceImage(
                modifier = modifier,
                image = errorRes,
                color = errorColor,
                contentScale = contentScale,
                contentDescription = contentDescription,
                tint = tint,
                alpha = alpha
            )
        }
    }
    OkImageImpl(
        modifier = modifier,
        image = image,
        contentDescription = contentDescription,
        isGif = isGif,
        contentScale = contentScale,
        crossFade = crossFade,
        errorColor = errorColor,
        errorRes = errorRes,
        placeholderColor = placeholderColor,
        placeholderRes = placeholderRes,
        tint = tint,
        alpha = alpha
    )
}

@Composable
internal expect fun OkImageImpl(
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
)

@Composable
internal expect fun getCoilImageRequestBuilder(): ImageRequest.Builder

@Composable
fun ResourceImage(
    modifier: Modifier,
    image: ResourceOr?,
    color: Color?,
    contentScale: ContentScale,
    contentDescription: String,
    tint: Color?,
    alpha: Float,
) {
    when (image) {
        is ImageResource -> {
            Image(
                modifier = modifier,
                painter = painterResource(image),
                contentScale = contentScale,
                contentDescription = contentDescription,
                colorFilter = if (tint == null) null else ColorFilter.tint(tint),
                alpha = alpha
            )
        }
        is ImageVector -> {
            Image(
                modifier = modifier,
                imageVector = image,
                contentScale = contentScale,
                contentDescription = contentDescription,
                colorFilter = if (tint == null) null else ColorFilter.tint(tint),
                alpha = alpha
            )
        }
        is Bitmap -> {
            Image(
                modifier = modifier,
                bitmap = image.asComposeImageBitmap(),
                contentScale = contentScale,
                contentDescription = contentDescription,
                colorFilter = if (tint == null) null else ColorFilter.tint(tint),
                alpha = alpha
            )
        }

        else -> {
            if (color != null) {
                Box(
                    modifier = modifier
                        .background(color)
                        .alpha(alpha)
                        .semantics {
                            this.contentDescription = contentDescription
                            this.role = Role.Image
                        }
                )
            }
        }
    }
}