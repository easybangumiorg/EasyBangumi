package com.heyanle.easybangumi4.v2.ui.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.source_push.SourcePushViewModel
import com.heyanle.easybangumi4.ui.source_push.sourcePushTypeList
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.component.V2Section

/** V2 presentation for installing a JS source from a file, URL or source code. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SourcePushV2() {
    val navController = LocalNavController.current
    val viewModel = viewModel<SourcePushViewModel>()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        V2SecondaryHeader(
            title = stringResource(R.string.extension_push),
            onBack = navController::popBackStack,
            largeTitle = true,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item {
                V2Section(title = "从文件添加") {
                    V2ActionRow(
                        icon = Icons.Filled.Folder,
                        title = stringResource(R.string.extension_push_from_file),
                        subtitle = "选择设备中的 JS 番源文件并立即安装",
                        onClick = viewModel::chooseJSFile,
                    )
                }
            }

            item {
                V2Section(title = stringResource(R.string.extension_push_from_input)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            sourcePushTypeList.forEach { type ->
                                val selected = state.currentType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.changeType(type) },
                                    label = { Text(type.label()) },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Code,
                                                contentDescription = null,
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = V2Tokens.Surface,
                                        labelColor = V2Tokens.TextSecondary,
                                        selectedContainerColor = V2Theme.colors.accentContainer,
                                        selectedLabelColor = V2Tokens.TextPrimary,
                                        selectedLeadingIconColor = V2Theme.colors.accent,
                                    ),
                                )
                            }
                        }
                        Text(
                            text = state.currentType.desc(),
                            modifier = Modifier.padding(top = 12.dp, bottom = 7.dp),
                            color = V2Tokens.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                        OutlinedTextField(
                            value = state.text,
                            onValueChange = viewModel::changeText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 240.dp),
                            placeholder = { Text(state.currentType.desc()) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = V2Theme.colors.accent,
                                unfocusedBorderColor = V2Tokens.Divider,
                                focusedContainerColor = V2Tokens.Surface,
                                unfocusedContainerColor = V2Tokens.Surface,
                            ),
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.navigationBarsPadding(),
            color = V2Tokens.Surface,
            tonalElevation = 0.dp,
        ) {
            Button(
                onClick = viewModel::push,
                enabled = state.text.isNotBlank() && state.dialog !is SourcePushViewModel.Dialog.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = V2Theme.colors.accent,
                    contentColor = V2Theme.colors.onAccent,
                    disabledContainerColor = V2Tokens.Divider,
                    disabledContentColor = V2Tokens.TextSecondary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.js_extension_push),
                    modifier = Modifier.padding(vertical = 5.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    when (val dialog = state.dialog) {
        is SourcePushViewModel.Dialog.Loading -> AlertDialog(
            onDismissRequest = {},
            containerColor = V2Tokens.Surface,
            title = { Text("正在添加番源", color = V2Tokens.TextPrimary) },
            text = { Text(dialog.msg, color = V2Tokens.TextSecondary) },
            confirmButton = {
                TextButton(onClick = viewModel::cancelCurrent) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.Error)
                }
            },
        )

        is SourcePushViewModel.Dialog.ErrorOrCompletely -> AlertDialog(
            onDismissRequest = viewModel::cleanErrorOrCompletely,
            containerColor = V2Tokens.Surface,
            text = { Text(dialog.msg, color = V2Tokens.TextPrimary) },
            confirmButton = {
                TextButton(onClick = viewModel::cleanErrorOrCompletely) {
                    Text(stringResource(R.string.confirm), color = V2Theme.colors.accent)
                }
            },
        )

        null -> Unit
    }
}
