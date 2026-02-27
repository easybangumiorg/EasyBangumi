package org.easybangumi.next.shared.compose.home.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.bangumi.account_card.BangumiAccountCard
import org.easybangumi.next.shared.compose.common.BooleanPreferenceItem
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.preference.MainPreference
import org.easybangumi.next.shared.resources.Res
import org.koin.compose.koinInject

@Composable
fun More() {
    val navController = LocalNavController.current
    val mainPreference = koinInject<MainPreference>()
    LazyColumn(
        modifier = Modifier.statusBarsPadding()
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(64.dp))
                AsyncImage(Res.images.logo, contentDescription = "Logo", modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text(stringRes(Res.strings.app_name))
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
            }
        }
        item {
            Column {
                ListItem(
                    headlineContent = {
                        Text(text = "Bangumi 账号：", color = MaterialTheme.colorScheme.secondary)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                BangumiAccountCard(Modifier)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
        item {
            BooleanPreferenceItem(
                title = { Text(text = stringRes(Res.strings.in_private)) },
                subtitle = { Text(text = stringRes(Res.strings.in_private_msg)) },
                icon = {
                    Icon(
                        Icons.Filled.HistoryToggleOff,
                        contentDescription = stringRes(Res.strings.in_private)
                    )
                },
                preference = mainPreference.privateMode,
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(RouterPage.TagManage)
                },
                headlineContent = { Text(text = stringRes(Res.strings.tag_manage)) },
                leadingContent = {
                    Icon(Icons.Filled.Tag, contentDescription = stringRes(Res.strings.tag_manage))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        item {
            HorizontalDivider()
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(RouterPage.Setting)
                },
                headlineContent = { Text(text = stringRes(Res.strings.setting)) },
                leadingContent = {
                    Icon(Icons.Filled.Settings, contentDescription = stringRes(Res.strings.setting))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        item {
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(RouterPage.About)
                },
                headlineContent = { Text(text = stringRes(Res.strings.about)) },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = stringRes(Res.strings.about))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        item {
            if (platformInformation.isDebug) {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(RouterPage.Debug.HOME)
                    },
                    headlineContent = { Text(text = "Debug Mode") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}
