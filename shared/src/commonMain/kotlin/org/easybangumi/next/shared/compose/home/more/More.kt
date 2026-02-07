package org.easybangumi.next.shared.compose.home.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
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
import org.easybangumi.next.shared.foundation.image.AsyncImage
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
@Composable
fun More() {
    val navController = LocalNavController.current
    LazyColumn(
        modifier = Modifier.statusBarsPadding()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
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
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                )
                BangumiAccountCard(Modifier)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }

        }
        item {
            if (platformInformation.isDebug) {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(RouterPage.Debug.HOME)
                    },
                    headlineContent = {
                        Text(text = "Debug Mode")
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                )
            }
        }
    }



}