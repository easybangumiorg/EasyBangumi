package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

@Composable
internal fun V2SecondaryHeader(
    title: String,
    onBack: () -> Unit,
    largeTitle: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (largeTitle) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    V2Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = V2Theme.colors.accent,
                    )
                }
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                actions()
            }
            Text(
                text = title,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 14.dp),
                color = V2Tokens.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, top = 6.dp, end = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            V2Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = V2Theme.colors.accent,
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            color = V2Tokens.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        actions()
    }
}

@Composable
internal fun V2Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = V2Tokens.ScreenHorizontalPadding,
            top = 18.dp,
            bottom = 8.dp,
        ),
        color = V2Tokens.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Surface(
        modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding),
        color = V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
internal fun V2ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = V2Theme.colors.accentContainer,
            shape = RoundedCornerShape(9.dp),
        ) {
            V2Icon(
                imageVector = icon,
                contentDescription = null,
                tint = V2Theme.colors.accent,
                modifier = Modifier.padding(7.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                color = V2Tokens.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 3.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Text("›", color = V2Tokens.TextSecondary, fontSize = 26.sp)
        }
    }
}

@Composable
internal fun V2SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = V2Tokens.Divider,
    )
}
