package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/**
 * Created by HeYanLe on 2023/3/6 16:02.
 * https://github.com/heyanLE
 */

@Composable
fun GradientImage(
    modifier: Modifier = Modifier,
    image: Any?,
    contentDescription: String,
    gradientHeight: Dp,
) {

    BoxWithConstraints(
        modifier
    ) {
        OkImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99F }
                .drawWithContent {
                    drawContent()
                    val colors = listOf(Color.Black, Color.Transparent)
                    drawRect(
                        topLeft = Offset(0F, maxHeight.toPx()-gradientHeight.toPx()),
                        size = Size(maxWidth.toPx(), gradientHeight.toPx()),
                        brush = Brush.verticalGradient(colors),
                        blendMode = BlendMode.DstIn
                    )
                },
            image = image,
            contentDescription = contentDescription,
            errorColor = Color.Transparent,
            placeholder = Color.Transparent,
        )
    }


}