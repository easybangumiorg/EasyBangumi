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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.main.more.EasyBangumiCard
import com.heyanle.easybangumi4.utils.openUrl
import com.microsoft.appcenter.distribute.Distribute

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
                            imageVector = Icons.Filled.ArrowBack, stringResource(id = com.heyanle.easy_i18n.R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
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

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        Distribute.checkForUpdate()
                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.check_update))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Upload,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.check_update)
                        )
                    },
                )

                Divider()

                val manager: ClipboardManager = LocalClipboardManager.current
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        manager.setText(AnnotatedString("729848189"))
                        "复制成功".moeSnackBar()
                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.qq_groud))
                    },
                    trailingContent = {
                        Text(text = "729848189")
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.qq),
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.qq_groud)
                        )
                    },
                )
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        manager.setText(AnnotatedString("370345983"))
                        "复制成功".moeSnackBar()
                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.qq_groud))
                    },
                    trailingContent = {
                        Text(text = "370345983")
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.qq),
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.qq_groud)
                        )
                    },
                )

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        kotlin.runCatching {
                            "https://pd.qq.com/s/4q8rd0285".openUrl()
                        }.onFailure {
                            it.printStackTrace()
                        }

                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.qq_chanel))
                    },
                    trailingContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_add))
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.qq),
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.qq_chanel)
                        )
                    },
                )

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        kotlin.runCatching {
                            "https://t.me/easybangumi".openUrl()
                        }.onFailure {
                            it.printStackTrace()
                        }

                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.telegram))
                    },
                    trailingContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_add))
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.telegram)
                        )
                    },
                )

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.clickable {
                        kotlin.runCatching {
                            "https://github.com/easybangumiorg/EasyBangumi".openUrl()
                        }.onFailure {
                            it.printStackTrace()
                        }

                    },
                    headlineContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.github))
                    },
                    trailingContent = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_explore))
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_explore)
                        )
                    },
                )

            }
        }
    }

}