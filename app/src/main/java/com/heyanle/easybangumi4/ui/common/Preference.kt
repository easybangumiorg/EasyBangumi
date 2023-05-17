package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
) {

    val value by preference.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()

    ListItem(
        modifier = modifier.clickable {
            scope.launch {
                preference.set(!value)
            }
        },
        headlineContent = title,
        leadingContent = icon,
        supportingContent = subtitle,
        trailingContent = {
            Switch(checked = value, onCheckedChange = {
                scope.launch {
                    preference.set(it)
                }
            })
        }

    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntPreferenceItem(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit),
    icon: @Composable (() -> Unit)? = null,
    textList: List<String>,
    preference: Preference<Int>,
    onChange: (Int)->Unit = {},
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    val value by preference.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()

    ListItem(
        modifier = modifier.clickable {
            showDialog = true
        },
        headlineContent = title,
        leadingContent = icon,
        supportingContent = {
            Text(text = textList.get(value))
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            },
            title = title,
            text = {
                LazyColumn(){
                    items(textList.size){
                        Row(
                            modifier = Modifier.clickable {
                                scope.launch {
                                    showDialog = false
                                    preference.set(it)
                                    onChange(it)
                                }
                            }
                        ) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = textList[it])
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(selected = it == value, onClick = {
                                scope.launch {
                                    showDialog = false
                                    preference.set(it)
                                    onChange(it)
                                }
                            })
                            Spacer(modifier = Modifier.size(4.dp))
                        }
                    }
                }
            }
        )
    }

}