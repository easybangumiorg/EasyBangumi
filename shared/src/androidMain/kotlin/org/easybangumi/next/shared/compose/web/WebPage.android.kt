package org.easybangumi.next.shared.compose.web

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.view_model.vm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun WebPage(param: WebPageParam) {
    val vm = vm(::WebPageVMAndroid, param)
    val nav = LocalNavController.current

    val state = vm.ui.value

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
            navigationIcon = {
                IconButton(
                    onClick = {
                        nav.popBackStack()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        vm.reload()
                    }
                ) {
                    Icon(Icons.Filled.Refresh, "refresh")
                }
            }
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (state) {
                is WebPageVMAndroid.State.Idle -> {
                    // 等待状态，可以显示提示信息
                }
                is WebPageVMAndroid.State.LoadingWebView -> {
                    Box(Modifier.fillMaxSize()) {
                        LoadingElements(
                            modifier = Modifier.fillMaxSize(),
                            isRow = false,
                            loadingMsg = "WebView 组件加载中..."
                        )
                    }
                }
                is WebPageVMAndroid.State.ShowWebView -> {
                    Column {
                        if (state.tips != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
                            ) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = state.tips, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        Box(Modifier.fillMaxWidth().weight(1f)) {
                            AndroidView(
                                factory = { context ->
                                    state.webView.apply {
                                        (this.parent as? ViewGroup)?.removeView(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                is WebPageVMAndroid.State.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMsg,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
