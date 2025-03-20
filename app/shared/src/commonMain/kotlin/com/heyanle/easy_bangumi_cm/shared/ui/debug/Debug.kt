package com.heyanle.easy_bangumi_cm.shared.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.heyanle.easy_bangumi_cm.common.foundation.ScrollableHeaderBehavior
import com.heyanle.easy_bangumi_cm.common.foundation.ScrollableHeaderScaffold

/**
 * Created by heyanlin on 2025/2/27.
 */
@Composable
fun Debug() {

    val behavior = ScrollableHeaderBehavior.enterAlwaysScrollBehavior()
    ScrollableHeaderScaffold(modifier = Modifier.fillMaxSize(),
        behavior = behavior,
        headerIfBehavior = {
            Box(Modifier.fillMaxWidth().background(Color.White)) {
                Text("title")
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = it
            ) {
                items(100) {
                    Text(modifier = Modifier.fillMaxWidth(), text = "item $it")
                }
            }
        }
    )




}