package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

internal enum class V2TabStyle {
    PrimaryUnderline,
    SecondaryDot,
    EqualUnderline,
}

/** Intrinsic-width, left-aligned tabs used by the V2 home surface. */
@Composable
internal fun V2ScrollableTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: V2TabStyle = V2TabStyle.PrimaryUnderline,
) {
    if (style == V2TabStyle.EqualUnderline) {
        Row(modifier = modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val labelColor by animateColorAsState(
                    targetValue = if (selected) V2Theme.colors.accent else V2Tokens.TextSecondary,
                    animationSpec = tween(180),
                    label = "v2-equal-tab-color",
                )
                val indicatorAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(180),
                    label = "v2-equal-tab-indicator",
                )
                val interactionSource = remember { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(horizontal = 4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true),
                            role = Role.Tab,
                        ) { onSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = label,
                        color = labelColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 9.dp),
                    )
                    Box(
                        modifier = Modifier
                            .width(V2Tokens.TabIndicatorWidth)
                            .height(2.dp)
                            .background(V2Theme.colors.accent.copy(alpha = indicatorAlpha)),
                    )
                }
            }
        }
        return
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = V2Tokens.ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(V2Tokens.TabSpacing),
    ) {
        itemsIndexed(labels) { index, label ->
            val selected = index == selectedIndex
            val dotAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = tween(180),
                label = "v2-secondary-tab-dot",
            )
            val primaryColor by animateColorAsState(
                targetValue = if (selected) V2Theme.colors.accent else V2Tokens.TextPrimary,
                animationSpec = tween(180),
                label = "v2-primary-tab-color",
            )
            val secondaryColor by animateColorAsState(
                targetValue = if (selected) V2Tokens.TextPrimary else V2Tokens.TextSecondary,
                animationSpec = tween(180),
                label = "v2-secondary-tab-color",
            )
            val indicatorAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = tween(180),
                label = "v2-primary-tab-indicator",
            )
            val interactionSource = remember { MutableInteractionSource() }
            if (style == V2TabStyle.SecondaryDot) {
                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true),
                            role = Role.Tab,
                        ) { onSelected(index) }
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                V2Theme.colors.accent.copy(alpha = dotAlpha),
                                androidx.compose.foundation.shape.CircleShape,
                            ),
                    )
                    Text(
                        text = label,
                        color = secondaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                return@itemsIndexed
            }
            Column(
                modifier = Modifier
                    .height(V2Tokens.MinimumTouchTarget)
                    .padding(horizontal = 4.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true),
                        role = Role.Tab,
                    ) { onSelected(index) },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = label,
                    color = primaryColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Box(
                    modifier = Modifier
                        .width(V2Tokens.TabIndicatorWidth)
                        .height(2.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .width(V2Tokens.TabIndicatorWidth)
                            .height(2.dp)
                            .background(V2Theme.colors.accent.copy(alpha = indicatorAlpha)),
                    )
                }
            }
        }
    }
}
