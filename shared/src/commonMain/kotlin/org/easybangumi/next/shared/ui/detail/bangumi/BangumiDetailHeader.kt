package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.ext.shared.plugin.bangumi.model.Subject
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard

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
fun BangumiDetailHeader(
    viewModel: BangumiDetailViewModel,
    subjectState: DataState<Subject>,
    modifier: Modifier,
) {



    Column {

        Row {

        }

    }
}

@Composable
fun BangumiDetailHeaderContent(
    modifier: Modifier,
    viewModel: BangumiDetailViewModel,
    subjectState: DataState.Ok<Subject>,
) {

}

@Composable
fun BangumiDetailHeaderLoading(
    modifier: Modifier,
) {

}