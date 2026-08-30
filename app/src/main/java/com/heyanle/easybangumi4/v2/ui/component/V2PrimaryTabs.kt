package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

/** Original V2 primary tabs. Secondary tabs use [V2ScrollableTabs] instead. */
@Composable
internal fun V2PrimaryTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = V2Tokens.ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(V2Tokens.TabSpacing),
    ) {
        itemsIndexed(labels) { index, label ->
            val selected = index == selectedIndex
            val interaction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .height(V2Tokens.MinimumTouchTarget)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(bounded = true),
                        role = Role.Tab,
                    ) { onSelected(index) },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = label,
                    color = if (selected) V2Theme.colors.accent else V2Tokens.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Box(
                    Modifier
                        .width(V2Tokens.TabIndicatorWidth)
                        .height(2.dp)
                        .background(if (selected) V2Theme.colors.accent else androidx.compose.ui.graphics.Color.Transparent),
                )
            }
        }
    }
}
