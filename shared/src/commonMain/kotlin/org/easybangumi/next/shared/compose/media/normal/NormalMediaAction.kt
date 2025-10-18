package org.easybangumi.next.shared.compose.media.normal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.easybangumi.next.shared.compose.media.bangumi.Action
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
// TODO
@Composable
fun NormalMediaActions(
    isStar: Boolean,
    isDownloading: Boolean,
    isDeleting: Boolean,
    isFromRemote: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onExtPlayer: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onBindBangumi: () -> Unit,
) {
    Row (
        Modifier.padding(4.dp, 0.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val starIcon = Icons.Filled.StarOutline
        val starTextId = Res.strings.started_miro
        // 点击追番
        Action(
            icon = {
                Icon(
                    starIcon,
                    stringRes(starTextId),
                    tint = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            },
            msg = {
                Text(
                    text = stringRes(starTextId),
                    color = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            },
            onClick = {
                onStar(!isStar)
            }
        )

        // 搜索同名番
        Action(
            icon = {
                Icon(
                    Icons.Filled.Search,
                    stringRes(Res.strings.search)
                )
            },
            msg = {
                Text(
                    text = stringRes(Res.strings.search),
                    fontSize = 12.sp
                )
            },
            onClick = onSearch
        )

//        if (showWeb) {
//            // 打开原网站
//            Action(
//                icon = {
//                    Icon(
//                        painterResource(id = R.drawable.ic_webview_24dp),
//                        stringRes(Res.stringsopen_source_url)
//                    )
//                },
//                msg = {
//                    Text(
//                        text = stringRes(Res.strings.open_source_url),
//                        fontSize = 12.sp
//                    )
//                },
//                onClick = onWeb
//            )
//        }

        if (isFromRemote) {
            // 下载
            Action(
                icon = {
                    Icon(
                        Icons.Filled.Download,
                        stringRes(Res.strings.download),
                        tint = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                msg = {
                    Text(
                        text = stringRes(Res.strings.download),
                        color = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                },
                onClick = onDownload
            )
        } else {

//            // 保存到本地
//            Action(
//                icon = {
//                    Icon(
//                        Icons.Filled.Save,
//                        stringResource(id = com.heyanle.easy_i18n.R.string.save),
//                        tint = if (isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
//                    )
//                },
//                msg = {
//                    Text(
//                        text = stringResource(id = com.heyanle.easy_i18n.R.string.save),
//                        color = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
//                        fontSize = 12.sp
//                    )
//                },
//                onClick = onSave
//            )
            // 删除
            Action(
                icon = {
                    Icon(
                        Icons.Filled.Delete,
                        stringRes(Res.strings.delete),
                        tint = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                msg = {
                    Text(
                        text = stringRes(Res.strings.delete),
                        color = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                },
                onClick = onDelete
            )
        }

        // 外部播放
        Action(
            icon = {
                Icon(
                    Icons.Filled.ScreenShare,
                    stringRes(Res.strings.ext_player)
                )
            },
            msg = {
                Text(
                    text = stringRes(Res.strings.ext_player),
                    fontSize = 12.sp
                )
            },
            onClick = onExtPlayer
        )
    }
}