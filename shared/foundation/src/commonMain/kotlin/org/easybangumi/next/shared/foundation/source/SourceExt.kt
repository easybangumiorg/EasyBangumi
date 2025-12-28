package org.easybangumi.next.shared.foundation.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.source.SourceCase
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

@Composable
fun sourceCase(): SourceCase {
    return koinInject()
}

@Composable
fun labelOrKey(key: String): ResourceOr {
    return label(key) ?: key
}

@Composable
fun label(key: String): ResourceOr? {
    val sourceCase = sourceCase()
    val flow = remember(sourceCase) {
        sourceCase.sourceManifestFlow()
    }
    val manifest = flow.collectAsState(emptyList()).value
    val item = remember(manifest, key) {
        manifest.firstOrNull { it.key == key }
    }
    return item?.label
}

@Composable
fun label(cartoonInfo: CartoonInfo): ResourceOr {
    val sourceKey = cartoonInfo.fromSourceKey
    return labelOrKey(sourceKey)
}
