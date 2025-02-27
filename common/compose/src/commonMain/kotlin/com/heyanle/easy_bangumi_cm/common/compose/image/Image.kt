package com.heyanle.easy_bangumi_cm.common.compose.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.skia.Bitmap

/**
 * Created by heyanlin on 2025/2/27.
 */
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
                    modifier = Modifier
                        .background(color)
                        .alpha(alpha)
                        .semantics {
                            this.contentDescription = contentDescription
                            this.role = Role.Image
                        }.then(modifier)
                )
            }
        }
    }
}