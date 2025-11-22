package org.easybangumi.next.shared.debug.bangumi

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.debug.DebugScope
import org.koin.compose.koinInject

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
fun DebugScope.BangumiDebug() {

    val accountController: BangumiAccountController = koinInject()

    var username by remember {
        mutableStateOf(accountController.accountInfoFlow.value.username)
    }
    var token by remember {
        mutableStateOf(accountController.accountInfoFlow.value.token)
    }


    Column {
        TextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                androidx.compose.material3.Text("Username")
            }
        )
        TextField(
            value = token,
            onValueChange = {
                token = it
            },
            label = {
                androidx.compose.material3.Text("Token")
            }
        )

        androidx.compose.material3.Button(onClick = {
            accountController.updateAccountInfo(
                BangumiAccountController.BangumiAccountInfo(
                    token = token,
                    username = username
                )
            )
        }) {
            androidx.compose.material3.Text("Update Account Info")
        }
    }

}