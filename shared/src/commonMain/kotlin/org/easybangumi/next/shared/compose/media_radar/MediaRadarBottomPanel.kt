package org.easybangumi.next.shared.compose.media_radar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.easybangumi.next.lib.logger.logger

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
private val logger = logger("MediaRadarBottomPanel")
@Composable
fun MediaRadarBottomPanel(
    vm: MediaRadarViewModel,
    show: Boolean = true,
    onDismissRequest: () -> Unit,
    onSelection: (MediaRadarViewModel.SelectionResult?) -> Unit = { _ -> }
) {
    val bottomSheet = rememberModalBottomSheetState(false)
    LaunchedEffect(show, bottomSheet.isVisible) {
        logger.info("MediaRadarBottomPanel LaunchedEffect show: $show")
        if (show && !bottomSheet.isVisible) {
            bottomSheet.show()
        } else if (!show) {
            bottomSheet.hide()
        }
    }
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = bottomSheet,
            contentWindowInsets = {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Top)
            }
        ) {
            MediaRadar(
                modifier = Modifier.fillMaxSize(),
                vm = vm,
                onSelection = onSelection
            )
        }
    }

}