package org.easybangumi.next.shared.foundation.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.easybangumi.next.shared.resources.ResourceOr

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
    clipToBounds: Boolean
) {
    TODO("Not yet implemented")
}