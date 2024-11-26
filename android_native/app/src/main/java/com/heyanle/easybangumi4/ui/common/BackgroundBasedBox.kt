package com.heyanle.easybangumi4.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

/**
 * Created by LoliBall on 2023/3/1 13:41.
 * https://github.com/WhichWho
 */
@Composable
fun BackgroundBasedBox(
    modifier: Modifier = Modifier,
    background: @Composable () -> Unit,
    foreground: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            background()
            foreground()
        }
    ) { measurables, constraints ->
        val back = measurables[0].measure(constraints)
        val backConstraints = Constraints(maxWidth = back.width, maxHeight = back.height)
        val fore = measurables[1].measure(backConstraints)
        layout(back.width, back.height) {
            back.place(0, 0)
            fore.place(0, 0)
        }
    }
}