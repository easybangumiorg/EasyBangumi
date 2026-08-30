package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.v2.theme.V2Theme

@Composable
internal fun V2CountBadge(count: Int) {
    Surface(
        modifier = Modifier.size(30.dp),
        color = V2Theme.colors.accentContainer,
        contentColor = V2Theme.colors.accent,
        shape = CircleShape,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                fontSize = if (count > 99) 9.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
