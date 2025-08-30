package org.easybangumi.next.shared.compose.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetail

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
fun Detail(cartoonIndex: CartoonIndex) {
    val nav = LocalNavController.current
    when (cartoonIndex.source) {
        BangumiInnerSource.SOURCE_KEY -> {
            BangumiDetail(
                cartoonIndex = cartoonIndex,
                contentPaddingTop = LocalSizeScheme.current.statusBarHeight,
                onBack = {
                    nav.popBackStack()
                }
            )
        }
    }
}
