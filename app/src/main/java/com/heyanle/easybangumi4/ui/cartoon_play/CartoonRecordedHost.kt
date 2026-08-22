package com.heyanle.easybangumi4.ui.cartoon_play

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.CartoonRecorded
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.CartoonRecordedModel
import loli.ball.easyplayer2.ControlViewModel

internal object CartoonRecordedHostTestTags {
    const val OVERLAY = "cartoon_recorded_overlay"
}

/**
 * Owns the route-level hand-off between the normal player surface and the clipping surface.
 *
 * The regular playback content intentionally remains composed underneath this host. This preserves
 * full-screen and player lifecycle state while [CartoonRecorded] temporarily owns video output.
 */
@Composable
internal fun CartoonRecordedHost(
    controlViewModel: ControlViewModel,
    recording: CartoonRecordedModel?,
    onDismissRequest: () -> Unit,
) {
    RecordingOverlayHost(
        recording = recording,
        modeSyncKey = controlViewModel,
        onRecordingModeChanged = { isRecording ->
            if (isRecording) {
                controlViewModel.unbind()
            } else {
                controlViewModel.bind()
            }
        },
        onDismissRequest = onDismissRequest,
    ) { model, dismiss ->
        CartoonRecorded(
            controlViewModel = controlViewModel,
            cartoonRecordedModel = model,
            show = true,
            onDismissRequire = dismiss,
        )
    }
}

/**
 * Testable state-to-overlay boundary. A change between two non-null models keeps the same recording
 * mode and therefore does not repeat the surface hand-off.
 */
@Composable
internal fun <T : Any> RecordingOverlayHost(
    recording: T?,
    modeSyncKey: Any? = Unit,
    onRecordingModeChanged: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    overlay: @Composable (recording: T, onDismissRequest: () -> Unit) -> Unit,
) {
    val isRecording = recording != null
    val currentModeChanged by rememberUpdatedState(onRecordingModeChanged)
    val previousMode = remember(modeSyncKey) { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(modeSyncKey, isRecording) {
        val previous = previousMode.value
        previousMode.value = isRecording

        // EasyPlayerStateSync already binds the normal surface when the route is first composed.
        if (previous == null && !isRecording) return@LaunchedEffect
        if (previous == isRecording) return@LaunchedEffect

        runCatching {
            currentModeChanged(isRecording)
        }.onFailure {
            Log.e(TAG, "Failed to switch player surface for recording mode=$isRecording", it)
        }
    }

    recording?.let { model ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag(CartoonRecordedHostTestTags.OVERLAY),
        ) {
            overlay(model, onDismissRequest)
        }
        // Registered after EasyPlayerStateSync so Back exits clipping before it exits full screen.
        BackHandler(onBack = onDismissRequest)
    }
}

private const val TAG = "CartoonRecordedHost"
