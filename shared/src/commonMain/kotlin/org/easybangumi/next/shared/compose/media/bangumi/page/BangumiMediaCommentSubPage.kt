package org.easybangumi.next.shared.compose.media.bangumi.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaCommonVM
import org.easybangumi.next.shared.foundation.elements.ErrorElements

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
fun BangumiMediaCommentSubPage(
    vm: BangumiMediaCommonVM
) {
    ErrorElements(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        isRow = false,
        errorMsg = "加紧施工中，敬请期待！"
    )
}