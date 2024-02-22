package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.EmumPreferenceItem
import com.heyanle.easybangumi4.ui.common.StringSelectPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlin.math.max

/**
 * Created by HeYanLe on 2023/8/5 23:02.
 * https://github.com/heyanLE
 */
@Composable
fun ColumnScope.PlayerSetting(
    nestedScrollConnection: NestedScrollConnection
) {

    val nav = LocalNavController.current

    val scope = rememberCoroutineScope()

    val settingPreferences: SettingPreferences by Injekt.injectLazy()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        BooleanPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.use_external_player)) },
            preference = settingPreferences.useExternalVideoPlayer
        )

        BooleanPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.player_bottom_nav_padding)) },
            preference = settingPreferences.playerBottomNavigationBarPadding
        )

        EmumPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.player_orientation_mode)) },
            textList = listOf(
                stringResource(id = com.heyanle.easy_i18n.R.string.auto),
                stringResource(id = com.heyanle.easy_i18n.R.string.always_on),
                stringResource(id = com.heyanle.easy_i18n.R.string.always_off),
            ),
            preference = settingPreferences.playerOrientationMode,
            onChangeListener = {

            }
        )


        val sizePre by settingPreferences.cacheSize.flow()
            .collectAsState(settingPreferences.cacheSize.get())
        val size = settingPreferences.cacheSizeSelection
        StringSelectPreferenceItem(
            title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.max_cache_size)) },
            textList = size.map { it.second },
            select = size.indexOfFirst { it.first == sizePre }.let { if (it == -1) 0 else it }
        ) {
            settingPreferences.cacheSize.set(size[it].first)
            stringRes(com.heyanle.easy_i18n.R.string.should_reboot).moeSnackBar()
        }

        val fastWeight by settingPreferences.fastWeight.flow()
            .collectAsState(initial = settingPreferences.fastWeight.get())
        val fastSecond by settingPreferences.fastSecond.flow()
            .collectAsState(initial = settingPreferences.fastSecond.get())

        Row(
            modifier = Modifier
                .clickable {
                    if (fastWeight == 0) {
                        settingPreferences.fastWeight.set(5)
                    } else {
                        settingPreferences.fastWeight.set(-fastWeight)
                    }
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(id = R.string.double_tap_fast),
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = fastWeight > 0, onCheckedChange = {
                if (fastWeight == 0) {
                    settingPreferences.fastWeight.set(5)
                } else {
                    settingPreferences.fastWeight.set(-fastWeight)
                }
            })
        }



        if (fastWeight > 0f) {

            Slider(
                value = max(
                    settingPreferences.fastWeightSelection.indexOf(fastWeight).toFloat(),
                    0F
                ),
                onValueChange = {
                    val index = it.toInt()
                    val r = settingPreferences.fastWeightSelection.getOrNull(index)
                    r?.let {
                        settingPreferences.fastWeight.set(it)
                    }
                },
                steps = settingPreferences.fastWeightSelection.size - 2,
                valueRange = 0F..settingPreferences.fastWeightSelection.size.toFloat() - 1
            )

            Box(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)

            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(1f / fastWeight)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.FastRewind,
                        modifier = Modifier.align(Alignment.Center),
                        contentDescription = stringResource(id = R.string.long_press_fast_forward),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }



                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .fillMaxWidth(1f / fastWeight)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.FastForward,
                        modifier = Modifier.align(Alignment.Center),
                        contentDescription = stringResource(id = R.string.long_press_fast_forward),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (fastWeight == 2) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }

            var isFastTimeDialog by remember {
                mutableStateOf(false)
            }

            ListItem(
                modifier = Modifier.clickable {
                    isFastTimeDialog = true
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.fast_time))
                },
                supportingContent = {
                    Text(text = "${fastSecond}s")
                }
            )

            if (isFastTimeDialog) {
                val focusRequest = remember {
                    FocusRequester()
                }
                val text = remember {
                    mutableStateOf(fastSecond.toString())
                }
                DisposableEffect(key1 = Unit) {
                    runCatching {
                        focusRequest.requestFocus()
                    }.onFailure {
                        it.printStackTrace()
                    }
                    onDispose {
                        runCatching {
                            focusRequest.freeFocus()
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                }
                AlertDialog(
                    onDismissRequest = {
                        isFastTimeDialog = false
                    },
                    title = {
                        Text(text = stringRes(R.string.fast_time))
                    },
                    text = {
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(focusRequest),
                            value = text.value,
                            onValueChange = { s ->
                                if (s.none {
                                        it < '0' || it > '9'
                                    }) {
                                    text.value = s
                                }
                            })
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val tex = text.value
                            val f = tex.toIntOrNull() ?: -1
                            if (f <= 0) {
                                stringRes(R.string.please_input_right_speed).moeSnackBar()
                            } else {
                                settingPreferences.fastSecond.set(f)
                            }
                            isFastTimeDialog = false
                        }) {
                            Text(text = stringRes(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            isFastTimeDialog = false
                        }) {
                            Text(text = stringRes(R.string.cancel))
                        }
                    }
                )
            }

        }
    }
}