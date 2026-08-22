package com.heyanle.easybangumi4.v2.ui.web

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** V2 host for a verification WebView created and owned by the source verification flow. */
@Composable
internal fun WebVerificationV2(
    webView: WebView,
    tips: String,
    onCheck: (WebView) -> Boolean,
    onStop: (WebView) -> Unit,
) {
    val navController = LocalNavController.current

    LaunchedEffect(webView, onCheck) {
        while (isActive) {
            if (onCheck(webView)) {
                navController.popBackStack()
                return@LaunchedEffect
            }
            delay(1_000)
        }
    }
    DisposableEffect(webView, onStop) {
        onDispose { onStop(webView) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        V2SecondaryHeader(
            title = "网页验证",
            onBack = navController::popBackStack,
        )
        if (tips.isNotBlank()) {
            Text(
                text = tips,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(V2Theme.colors.accentContainer)
                    .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 10.dp),
                color = V2Tokens.TextPrimary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
