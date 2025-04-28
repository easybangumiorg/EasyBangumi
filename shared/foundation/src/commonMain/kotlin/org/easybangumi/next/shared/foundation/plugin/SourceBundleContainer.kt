package org.easybangumi.next.shared.foundation.plugin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.plugin.core.source.SourceBundle
import org.easybangumi.next.shared.plugin.core.source.SourceController
import org.koin.compose.koinInject

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
val LocalSourceBundle = staticCompositionLocalOf<SourceBundle> {
    error("SourceBundle Not Provide")
}


@Composable
fun SourceBundleContainer(
    modifier: Modifier,
    content: @Composable (SourceBundle) -> Unit
) {
    val sourceController: SourceController = koinInject()
    val sourceBundleState = sourceController.sourceBundleFlow.collectAsState()
    val sourceBundle = sourceBundleState.value
    LoadScaffold(
        modifier = modifier,
        data = sourceBundle,
        errorRetry = {
            sourceController.refresh()
        },
        onEmptyIfCheck = {
            SourceEmpty()
        }
    ) {
        CompositionLocalProvider(LocalSourceBundle provides it.data) {
            content(it.data)
        }
    }

}

@Composable
fun SourceEmpty() {}