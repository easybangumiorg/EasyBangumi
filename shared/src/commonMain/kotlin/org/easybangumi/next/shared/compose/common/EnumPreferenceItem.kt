package org.easybangumi.next.shared.compose.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.store.preference.Preference

@Composable
inline fun <reified T : Enum<T>> EnumPreferenceItem(
    noinline title: @Composable () -> Unit,
    textList: List<String>,
    preference: Preference<T>,
    noinline onChangeListener: ((T) -> Unit)? = null,
) {
    val currentValue by preference.flow().collectAsState(preference.get())
    val enumValues = enumValues<T>()

    ListItem(
        headlineContent = title,
        supportingContent = {
            Column {
                enumValues.forEachIndexed { index, value ->
                    val label = textList.getOrElse(index) { value.name }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                preference.set(value)
                                onChangeListener?.invoke(value)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentValue == value,
                            onClick = {
                                preference.set(value)
                                onChangeListener?.invoke(value)
                            }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
