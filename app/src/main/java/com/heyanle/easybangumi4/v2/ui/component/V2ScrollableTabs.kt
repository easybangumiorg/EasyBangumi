package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

internal enum class V2TabStyle { PrimaryUnderline, SecondaryDot, EqualUnderline }

/** V2-owned copy of the V1 ScrollableTabRow, mapped to V2 semantic colors. */
@Composable
internal fun V2ScrollableTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") style: V2TabStyle = V2TabStyle.PrimaryUnderline,
    badges: List<Int> = emptyList(),
) {
    if (labels.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(labels.indices)
    ScrollableTabRow(
        modifier = modifier.fillMaxWidth(),
        selectedTabIndex = safeIndex,
        containerColor = V2Tokens.WarmBackground,
        contentColor = V2Tokens.TextPrimary,
        edgePadding = 0.dp,
        divider = {},
        indicator = { positions ->
            if (safeIndex in positions.indices) TabIndicator(positions[safeIndex])
        },
    ) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = index == safeIndex,
                onClick = { onSelected(index) },
                text = {
                    val badgeCount = badges.elementAtOrNull(index) ?: 0
                    if (badgeCount > 0) {
                        BadgedBox(badge = { V2TabBadge(badgeCount) }) {
                            V2TabLabelText(label, selected = index == safeIndex)
                        }
                    } else {
                        V2TabLabelText(label, selected = index == safeIndex)
                    }
                },
            )
        }
    }
}

@Composable
internal fun V2TabLabelText(label: String, selected: Boolean) {
    Text(
        text = label,
        color = if (selected) V2Theme.colors.accent else V2Tokens.TextSecondary,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
internal fun V2TabBadge(count: Int) {
    Badge(
        containerColor = V2Theme.colors.accent,
        contentColor = V2Tokens.Surface,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
