package com.heyanle.easybangumi4.ui.storage.backup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage

/**
 * Created by heyanlin on 2024/4/29.
 */


@Composable
fun Backup() {

    val vm = viewModel<BackupViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value

    if (sta.showBackupDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissBackupDialog() },
            text = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.sure_to_backup))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.onBackup()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.dismissBackupDialog()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            },
        )
    }

    if (sta.isBackupDoing) {
        LoadingPage(
            modifier = Modifier.fillMaxSize(),
            loadingMsg = stringResource(id = com.heyanle.easy_i18n.R.string.backup_doing)
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                item {
                    ListItem(
                        modifier = Modifier.clickable {
                            vm.setNeedBackupCartoonData(!sta.needBackupCartoonData)
                        },
                        headlineContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cartoon_data) + if (sta.cartoonCount > 0) "（共 ${sta.cartoonCount.toString()} 项数据）" else "")
                        },
                        leadingContent = {
                            OkImage(
                                image =  R.mipmap.logo_new,
                                modifier = Modifier.size(40.dp),
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.cartoon_data)
                            )
                        },
                        supportingContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cartoon_data_desc))
                        },
                        trailingContent = {
                            Checkbox(checked = sta.needBackupCartoonData, onCheckedChange = {
                                vm.setNeedBackupCartoonData(it)
                            })
                        }
                    )
                }
                item {
                    ListItem(
                        modifier = Modifier.clickable {
                            vm.setNeedBackupPreferenceData(!sta.needBackupPreferenceData)
                        },
                        headlineContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.preference_data))
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Settings,
                                modifier = Modifier.size(40.dp),
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.preference_data)
                            )
                        },
                        supportingContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.preference_desc))
                        },
                        trailingContent = {
                            Checkbox(checked = sta.needBackupPreferenceData, onCheckedChange = {
                                vm.setNeedBackupPreferenceData(it)
                            })
                        }
                    )
                }

                item {
                    HorizontalDivider()
                }


                item {
                    ListItem(
                        modifier = Modifier.clickable {
                            vm.setNeedBackupExtension(!sta.needBackupExtension)
                        },
                        headlineContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension))
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Extension,
                                modifier = Modifier.size(40.dp),
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.extension)
                            )
                        },
                        supportingContent = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_desc))
                        },
                        trailingContent = {
                            Switch(checked = sta.needBackupExtension, onCheckedChange = {
                                vm.setNeedBackupExtension(it)
                            })
                        }
                    )

                }

                if (sta.needBackupExtension) {
                    items(sta.extensionList, key = {it.key}){ extension ->
                        ListItem(
                            modifier = Modifier.clickable {
                                vm.toggleExtensionPackage(extension)
                            },
                            headlineContent = {
                                Text(text = extension.label)
                            },
                            leadingContent = {
                                OkImage(
                                    modifier = Modifier.size(40.dp),
                                    image = extension.icon,
                                    crossFade = false,
                                    placeholderColor = null,
                                    placeholderRes = null,
                                    errorColor = null,
                                    errorRes = null,
                                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.extension_desc)
                                )
                            },
                            supportingContent = {
                                Text(text = extension.versionName)
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = sta.needExtensionPackage.contains(extension),
                                    onCheckedChange = {
                                        vm.toggleExtensionPackage(extension)
                                    })
                            }
                        )
                    }
                }


            }

            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp, 40.dp),
                    text = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.start_backup))
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.start_backup)
                        )
                    },
                    onClick = {
                        vm.showBackupDialog()
                    }
                )
            }
        }


    }


}