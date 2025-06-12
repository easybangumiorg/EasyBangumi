package com.easybangumi.next.shared.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.easybangumi.next.shared.debug.player.PlayerDebug
import kotlinx.serialization.Serializable

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
@Serializable
enum class DebugPage(
    val label: String
) {
    HOME("Debug Home"),
    PLAYER("Player Debug"),
}

class DebugScope (
    val onNav: (DebugPage) -> Unit,
    val onBack: () -> Unit = { /* no-op */ }
)

@Composable
fun DebugHost(
    debugPage: DebugPage,
    onNav: (DebugPage) -> Unit,
    onBack: () -> Unit = { /* no-op */ }
){
    DebugContainer(debugPage, onNav, onBack) {
        remember {
            DebugScope(onNav, onBack)
        }.apply {
            when (debugPage) {
                DebugPage.HOME -> DebugHome()
                DebugPage.PLAYER -> { PlayerDebug() }
            }
        }


    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugContainer(
    debugPage: DebugPage,
    onNav: (DebugPage) -> Unit,
    onBack: () -> Unit = { /* no-op */ },
    content: @Composable () -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text(debugPage.label)
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        onBack()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }
            }
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScope.DebugHome(
){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(DebugPage.entries) { dp ->
            ListItem(
                modifier = Modifier.clickable {
                    onNav(dp)
                },
                headlineContent = {
                    Text(dp.label)
                }
            )
        }
    }
}