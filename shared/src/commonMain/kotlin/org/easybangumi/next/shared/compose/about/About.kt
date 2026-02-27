package org.easybangumi.next.shared.compose.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.snackbar.moeSnackBar
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringRes(Res.strings.about)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringRes(Res.strings.back))
                }
            },
            scrollBehavior = scrollBehavior,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            // App card
            Column(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    Res.images.logo,
                    contentDescription = stringRes(Res.strings.app_name),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringRes(Res.strings.app_name))
            }

            HorizontalDivider()

            // Version
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(text = stringRes(Res.strings.version)) },
                leadingContent = {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = stringRes(Res.strings.version))
                },
                trailingContent = { Text(text = platformInformation.versionName) },
            )

            HorizontalDivider()

            // Website
            AboutUrlItem(
                title = stringRes(Res.strings.website),
                msg = stringRes(Res.strings.click_to_explore),
                onClick = { uriHandler.openUri("https://easybangumi.org") },
            )

            // QQ Channel
            AboutUrlItem(
                title = stringRes(Res.strings.qq_chanel),
                msg = stringRes(Res.strings.click_to_join),
                onClick = { uriHandler.openUri("https://pd.qq.com/s/4q8rd0285") },
            )

            // Telegram
            AboutUrlItem(
                title = stringRes(Res.strings.telegram),
                msg = stringRes(Res.strings.click_to_join),
                onClick = { uriHandler.openUri("https://t.me/easy_bangumi") },
            )

            // GitHub
            AboutUrlItem(
                title = stringRes(Res.strings.github),
                msg = stringRes(Res.strings.click_to_explore),
                onClick = { uriHandler.openUri("https://github.com/easybangumiorg/EasyBangumi") },
            )

            // QQ Group (copy)
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString("729848189"))
                    "复制成功".moeSnackBar()
                },
                headlineContent = { Text(text = stringRes(Res.strings.qq_groud)) },
                trailingContent = { Text(text = "729848189") },
                leadingContent = {
                    Icon(Icons.Filled.Public, contentDescription = stringRes(Res.strings.qq_groud))
                },
            )
        }
    }
}

@Composable
private fun AboutUrlItem(
    title: String,
    msg: String,
    onClick: () -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable {
            runCatching { onClick() }.onFailure { it.printStackTrace() }
        },
        headlineContent = { Text(text = title) },
        trailingContent = { Text(text = msg) },
        leadingContent = {
            Icon(Icons.Filled.Public, contentDescription = title)
        },
    )
}
