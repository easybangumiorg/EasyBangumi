package org.easybangumi.next.shared.compose.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.view_model.vm

@Composable
actual fun WebPage(param: WebPageParam) {
    val vm = vm(::WebPageVM, param)
    val nav  = LocalNavController.current

    val sta = vm.ui.value

    DisposableEffect(Unit) {
        onDispose {
            vm.onDisposableEffect()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (vm.onCheck()) {
                nav.popBackStack()
            }
        }
    }
    Column {
        TopAppBar(
            title = {
                if (vm.isBrowserLoading.value) {
                    CircularProgressIndicator(Modifier)
                }
            },
            navigationIcon =  {
                IconButton(
                    onClick = {
                        nav.popBackStack()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }
            },
            actions = {
//                if (sta is BangumiLoginVM.State.ShowJcef) {
//                    TextButton(
//                        onClick = {
//                            vm.reload()
//                        }
//                    ) {
//                        Text("申请授权")
//                    }
//                }
//                IconButton(onClick = {
//
//                }) {
//                    Icon(Icons.Filled.Help, null)
//                }

            }
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (sta) {
                is WebPageVM.State.Idle -> {

                }
                is WebPageVM.State.LoadingJcef -> {
                    Box(Modifier.fillMaxSize()) {
                        LoadingElements(
                            modifier = Modifier.fillMaxSize(),
                            isRow = false,
                            loadingMsg = "浏览器组件加载中..."
                        )
                    }
                }
                is WebPageVM.State.ShowJcef -> {
                    Column {
                        if (sta.tips != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
                            ) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = sta.tips, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }


                        Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                            SwingPanel(
                                factory = {
                                    sta.browser.uiComponent.apply {
                                        this.parent?.remove(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }


                }
                is WebPageVM.State.Error -> {}
            }
        }

    }

}