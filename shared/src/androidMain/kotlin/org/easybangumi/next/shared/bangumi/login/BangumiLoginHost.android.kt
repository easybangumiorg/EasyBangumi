package org.easybangumi.next.shared.bangumi.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.snackbar.moeSnackBar
import org.easybangumi.next.shared.foundation.view_model.vm

@Composable
actual fun BangumiLoginHost() {

    val vm = vm(::BangumiLoginVM)
    val nav  = LocalNavController.current

    val sta = vm.ui.value
    Column {
        TopAppBar(
            title = {},
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
                if (sta is BangumiLoginVM.State.ShowWebView) {
                    TextButton(
                        onClick = {
                            vm.reload()
                        }
                    ) {
                        Text("申请授权")
                    }
                }

            }
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (sta) {
                is BangumiLoginVM.State.Idle -> {

                }
                is BangumiLoginVM.State.ShowWebView -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "操作登录后点击右上角申请授权", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        val progress = remember(sta.webView.progress) {
                            sta.webView.progress
                        }
                        if (progress < 100) {
                            LoadingElements(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                isRow = true,
                                loadingMsg = "加载中... $progress%"
                            )
                        }
                        Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                            AndroidView(
                                factory = {
                                    sta.webView.apply {
                                        (this.parent as? android.view.ViewGroup)?.removeView(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }


                }
                is BangumiLoginVM.State.WaitingAccessToken -> {
                    Box(Modifier.fillMaxSize()) {
                        LoadingElements(
                            modifier = Modifier.fillMaxSize(),
                            isRow = false,
                            loadingMsg = "Waiting for access token..."
                        )
                    }
                }
                is BangumiLoginVM.State.GetAccountTokenSuccess -> {
                    LaunchedEffect(Unit) {
                        // TODO snackbar show success
                        "Bangumi 授权成功".moeSnackBar()
                        nav.popBackStack()
                    }
                }
                is BangumiLoginVM.State.ErrorAndExit -> {
                    LaunchedEffect(Unit) {
                        "授权错误： ${sta.errorMsg}".moeSnackBar()
                        nav.popBackStack()
                    }
                }
            }
        }

    }
}