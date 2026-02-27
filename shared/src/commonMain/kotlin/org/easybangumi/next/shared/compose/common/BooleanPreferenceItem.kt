package org.easybangumi.next.shared.compose.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.easybangumi.next.lib.store.preference.Preference

@Composable
fun BooleanPreferenceItem(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    preference: Preference<Boolean>,
) {
    val checked by preference.flow().collectAsState(preference.get())
    ListItem(
        modifier = Modifier.clickable { preference.set(!checked) },
        headlineContent = title,
        supportingContent = subtitle,
        leadingContent = icon,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { preference.set(it) }
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
