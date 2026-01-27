package org.easybangumi.next.shared.compose.home.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage

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
    Column {
        Spacer(Modifier.height(200.dp))
        if (platformInformation.isDebug) {
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(RouterPage.Debug.HOME)
                },
                headlineContent = {
                    Text(text = "Debug Mode")
                }
            )
        }
        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(RouterPage.BangumiLogin)
            },
            headlineContent = {
                Text(text = "BangumiLogin")
            }
        )

    }

}