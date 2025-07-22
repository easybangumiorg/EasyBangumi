package org.easybangumi.next.shared.media_radar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
fun MediaRadarBottomPanel(
    vm: MediaRadarViewModel,
    show: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(true)
        ) {
            MediaRadar(
                modifier = Modifier.fillMaxSize(),
                vm = vm,
            )
        }
    }



}