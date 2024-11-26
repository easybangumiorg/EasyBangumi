package com.heyanle.easybangumi4.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.C
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.main.more.EasyBangumiCard
import com.heyanle.easybangumi4.utils.openUrl

/**
 * Created by HeYanLe on 2023/4/1 23:06.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About() {

    val nav = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.about))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = com.heyanle.easy_i18n.R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
            ) {
                EasyBangumiCard()

                Divider()

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.version))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.version)
                        )
                    },
                    trailingContent = {
                        Text(text = BuildConfig.VERSION_NAME)
                    }

                )

                Divider()

                val manager: ClipboardManager = LocalClipboardManager.current

                C.aboutList.forEach {
                    when (it) {
                        is C.About.Url -> {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier.clickable {
                                    kotlin.runCatching {
                                        it.url.openUrl()
                                    }.onFailure {
                                        it.printStackTrace()
                                    }

                                },
                                headlineContent = {
                                    Text(text = it.title)
                                },
                                trailingContent = {
                                    Text(text = it.msg)
                                },
                                leadingContent = {
                                    OkImage(
                                        modifier = Modifier
                                            .size(24.dp),
                                        image = it.icon,
                                        contentDescription = it.title,
                                        crossFade = false,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                            )
                        }

                        is C.About.Copy -> {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier.clickable {
                                    manager.setText(AnnotatedString(it.copyValue))
                                    "复制成功".moeSnackBar()
                                },
                                headlineContent = {
                                    Text(text = it.title)
                                },
                                trailingContent = {
                                    Text(text = it.msg)
                                },
                                leadingContent = {
                                    OkImage(
                                        modifier = Modifier
                                            .size(24.dp),
                                        image = it.icon,
                                        contentDescription = it.title,
                                        crossFade = false,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}