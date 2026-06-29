package org.easybangumi.next.shared.foundation.scroll_header

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun DiscoverScrollHeaderScope.contentPointerScrollOpt(
    enabled: Boolean,
    modifier: Modifier
): Modifier {
    // LazyList 在滑动到最顶时在滑动滚轮不会发起嵌套滚动事件
    // 但是 scrollable 本身支持
    // 这里在外套一个 scrollable 处理这种 case
    if (enabled) {
        return modifier.scrollable(
            rememberScrollableState { 0f },
            enabled = enabled,
            orientation = Orientation.Vertical,
        )
    }
    return modifier
}