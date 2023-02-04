package com.heyanle.easybangumi.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/2/4 14:27.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewUser(
    webView: WebView,
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
                colors = TopAppBarDefaults.smallTopAppBarColors(
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
                            stringResource(id = R.string.back)
                        )
                    }
                },
            )
        },
        content = {
            Box(
                modifier = Modifier.padding(it)
            ) {
                AndroidView(factory = {
                    webView
                })
            }
        }
    )

}