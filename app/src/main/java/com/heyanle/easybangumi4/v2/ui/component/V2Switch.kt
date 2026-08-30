package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

/** Shared switch colors for every V2 page, including a visible off-state in both themes. */
@Composable
internal fun V2Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = V2Tokens.Surface,
            checkedTrackColor = V2Theme.colors.accent,
            uncheckedThumbColor = V2Tokens.Surface,
            uncheckedTrackColor = V2Tokens.SurfaceMuted,
            uncheckedBorderColor = V2Tokens.Divider,
            disabledUncheckedThumbColor = V2Tokens.SurfaceMuted,
            disabledUncheckedTrackColor = V2Tokens.Divider,
        ),
    )
}
