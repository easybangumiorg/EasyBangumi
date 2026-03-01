package org.easybangumi.next.shared.compose.browser

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.easybangumi.next.shared.foundation.view_model.vm

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun BrowserPage(
    param: BrowserPageParam,
    onBack: () -> Unit,
) {
    val vm = vm(::BrowserPageVMAndroid, param)
    val state = vm.ui.value


    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部工具栏
        if (param.showToolbar) {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 导航按钮
                        IconButton(
                            onClick = { vm.goBack() },
                            enabled = vm.canGoBack.value
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                        
                        IconButton(
                            onClick = { vm.goForward() },
                            enabled = vm.canGoForward.value
                        ) {
                            Icon(Icons.Default.ArrowForward, "Forward")
                        }
                        
                        IconButton(
                            onClick = { vm.reload() }
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                        
                        IconButton(
                            onClick = { vm.stopLoading() },
                            enabled = vm.isLoading.value
                        ) {
                            Icon(Icons.Default.Close, "Stop")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 地址栏
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = vm.urlInput.value,
                                onValueChange = { vm.urlInput.value = it },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (vm.urlInput.value.isEmpty()) {
                                    Text(
                                        text = "Enter URL...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                                it()
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                if (vm.urlInput.value.isNotBlank()) {
                                    vm.loadUrl(vm.urlInput.value)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Search, "Go")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    if (vm.isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        } else {
            // 简化工具栏，只有返回按钮
            TopAppBar(
                title = { Text(vm.pageTitle.value) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }

        // WebView 内容区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (state) {
                is BrowserPageVM.State.Initializing -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Initializing browser...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is BrowserPageVM.State.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading browser...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is BrowserPageVM.State.BrowserReady -> {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                vm.createWebView(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is BrowserPageVM.State.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}