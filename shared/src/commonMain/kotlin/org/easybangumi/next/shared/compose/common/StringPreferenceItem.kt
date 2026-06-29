package org.easybangumi.next.shared.compose.common

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.easybangumi.next.lib.store.preference.Preference

@Composable
fun StringPreferenceItem(
    title: @Composable () -> Unit,
    preference: Preference<String>,
    placeholder: String,
    supportingText: String? = null,
    isError: (String) -> Boolean = { false },
) {
    val currentValue by preference.flow().collectAsState(preference.get())
    var text by remember { mutableStateOf(currentValue) }

    LaunchedEffect(currentValue) {
        if (text != currentValue) {
            text = currentValue
        }
    }

    ListItem(
        headlineContent = title,
        supportingContent = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    preference.set(it)
                },
                placeholder = { Text(placeholder) },
                supportingText = supportingText?.let { message ->
                    { Text(message) }
                },
                isError = isError(text),
                singleLine = true,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
