package com.heyanle.easybangumi4.ui.common

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.heyanle.easybangumi4.preferences.Preference
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/22 16:06.
 * https://github.com/heyanLE
 */
@Composable
fun BooleanPreferenceItem(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit),
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    preference: Preference<Boolean>
){

    val value by preference.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()

    ListItem(
        modifier = modifier,
        headlineContent = title,
        leadingContent = icon,
        supportingContent = subtitle,
        trailingContent = {
            Switch(checked = value, onCheckedChange = {
                scope.launch{
                    preference.set(it)
                }
            })
        }

    )



}