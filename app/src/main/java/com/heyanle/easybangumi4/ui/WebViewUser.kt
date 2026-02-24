package com.heyanle.easybangumi4.ui

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Created by HeYanLe on 2023/2/4 14:27.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewUser(
    webView: WebView,
    tips: String? = null,
    onCheck: (WebView) -> Boolean,
    onStop: (WebView) -> Unit
) {

    val nav = LocalNavController.current

    LaunchedEffect(key1 = Unit) {
        while (isActive) {
            if (onCheck(webView)) {
                nav.popBackStack()
            }
            delay(1000)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            onStop(webView)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                modifier = Modifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {

                },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = com.heyanle.easy_i18n.R.string.back)
                        )
                    }
                },
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                if (tips != null && tips.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = tips, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                AndroidView(modifier = Modifier.fillMaxWidth().weight(1f),factory = {
                    webView
                })
            }
        }
    )

}