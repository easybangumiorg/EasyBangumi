package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Created by LoliBall on 2024/1/4 22:18.
 * https://github.com/WhichWho
 */
@Composable
fun ToggleButton(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        shape = ShapeDefaults.Small,
        colors = if (checked) ButtonDefaults.filledTonalButtonColors()
        else ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = if (checked) null else ButtonDefaults.outlinedButtonBorder,
        elevation = if (checked) ButtonDefaults.filledTonalButtonElevation() else null,
        modifier = modifier,
        content = content,
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}
