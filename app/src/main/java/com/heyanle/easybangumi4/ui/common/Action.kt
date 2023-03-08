package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Created by HeYanLe on 2023/3/8 10:40.
 * https://github.com/heyanLE
 */

@Composable
fun ActionRow (
    modifier: Modifier = Modifier,
    content: @Composable RowScope.()->Unit
) {
    Row (
        Modifier.padding(4.dp, 0.dp).then(modifier),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun RowScope.Action(
    icon: @Composable () -> Unit,
    msg: @Composable () -> Unit,
    onClick: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        msg()
    }
}