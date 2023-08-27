package com.heyanle.easybangumi4.compose.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Created by HeYanLe on 2023/7/30 11:03.
 * https://github.com/heyanLE
 */
@Composable
fun TabIndicator(currentTabPosition: TabPosition) {
    TabRowDefaults.Indicator(
        Modifier
            .tabIndicatorOffset(currentTabPosition)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
    )
}