package com.heyanle.easybangumi4.ui.extension_push

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by HeYanLe on 2024/10/27 14:14.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExtensionPush() {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val nav = LocalNavController.current
    val vm = viewModel<ExtensionPushViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    val fq = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.extension_push))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )

            val labelList = remember {
                listOf(
                    stringRes(R.string.js_file_url),
                    stringRes(R.string.js_code),
                    stringRes(R.string.js_repo)
                )
            }

            val descList = remember {
                listOf(
                    stringRes(R.string.js_file_url_desc),
                    stringRes(R.string.js_code_desc),
                    stringRes(R.string.js_repo_desc)
                )
            }



            LazyColumn(
                modifier = Modifier.fillMaxWidth().nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                // 选择文件按钮
                item {
                    FilledTonalButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            vm.chooseJSFile()
                        }
                    ) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_push_from_file))
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    ListItem(headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_push_from_input), color = MaterialTheme.colorScheme.primary)
                    })
                }

                item {


                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),

                    ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        repeat(labelList.size) {
                            FilterChip(
                                selected = sta.sourceType == it,
                                onClick = {
                                    vm.onSourceTypeChange(it)
                                },
                                label = { Text(text = labelList[it]) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(Dp.Unspecified, 300.dp)
                            .padding(16.dp)
                            .focusRequester(fq),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
//                        colors = TextFieldDefaults.colors(
//                            unfocusedContainerColor = Color.Transparent,
//                            focusedContainerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                        ),
                        value = sta.getText(),
                        onValueChange = {
                            vm.onTextChange(sta.sourceType, it)
                        },
                        placeholder = {
                            Text(
                                text = descList.getOrNull(sta.sourceType)?:"",
                                maxLines = 1
                            )
                        })
                }

                item { Spacer(Modifier.height(300.dp)) }

            }


        }
    }


}