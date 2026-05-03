package org.easybangumi.next.shared.playcon.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.api.action.VolumeAction
import org.easybangumi.next.libplayer.api.action.VolumeActionResult
import org.easybangumi.next.libplayer.vlcj.BaseVlcjPlayerBridge
import org.easybangumi.next.libplayer.vlcj.bitmap.VlcPlayerBitmapFrameState
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.libplayer.vlcj.bitmap.VlcjPlayerBitmapBridge
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconVM
import org.koin.core.component.inject

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
class DesktopPlayerVM(
    val fullscreenStrategy: FullscreenStrategy,
): BaseViewModel() {

    companion object {
        const val MEDIA_COMPONENT_ASPECT = 16f / 9f
        const val VOLUME_MIN = DesktopVolumeState.MIN
        const val VOLUME_MAX = DesktopVolumeState.MAX
        const val VOLUME_STEP = 5
        private const val VOLUME_PREF_KEY = "desktop_media_player_volume"
        private const val VOLUME_PERSIST_THROTTLE_MS = 250L
    }

    val preferenceStore: PreferenceStore by inject()
    private val volumePref = preferenceStore.getInt(VOLUME_PREF_KEY, 100)

    var volume by mutableIntStateOf(DesktopVolumeState.clamp(volumePref.get()))
        private set
    var mute by mutableStateOf(false)
        private set
    var isVolumeControlAvailable by mutableStateOf(true)
        private set
    var volumeControlFailureReason by mutableStateOf<VolumeActionResult.Reason?>(null)
        private set

    private var lastVolumeBeforeMute = volume.coerceAtLeast(20)
    private var isVolumeInitialized = false
    private var persistVolumeJob: Job? = null


    val vlcjManager: VlcjBridgeManager by inject()
    val vlcjPlayerBridge: BaseVlcjPlayerBridge by lazy {
        vlcjManager.getOrCreateBitmapBridge(this.toString())
    }
    val vlcPlayerBitmapFrameState = VlcPlayerBitmapFrameState()

    val playconVM: PointerPlayconVM by childViewModel {
        PointerPlayconVM(
            vlcjPlayerBridge,
        )
    }

    val playInfo = mutableStateOf<DataState<PlayInfo>>(DataState.none())
    val isFinalLoading = mutableStateOf(false)

    init {

//        vlcPlayerBitmapFrameState.bindBridge(vlcjPlayerBitmapBridge)
//        addCloseable(vlcPlayerBitmapFrameState)
//        addCloseable(vlcjPlayerBitmapBridge)

        vlcPlayerBitmapFrameState.bindBridge(vlcjPlayerBridge as VlcjPlayerBitmapBridge)
        addCloseable(vlcPlayerBitmapFrameState)
        addCloseable(vlcjPlayerBridge)

        initializeVolumeStateIfNeeded()

        viewModelScope.launch {
            snapshotFlow {
                fullscreenStrategy.isFullscreen()
            }.collectLatest {
                playconVM.screenMode = if (it) {
                    BasePlayconViewModel.ScreenMode.FULLSCREEN
                } else {
                    BasePlayconViewModel.ScreenMode.NORMAL
                }
            }
        }

        viewModelScope.launch {
            snapshotFlow {
                playInfo.value
            }.collectLatest {
                if (it.isLoading()) {
                    vlcjPlayerBridge.setPlayWhenReady(false)
                }
                val it = playInfo.value.okOrNull()
                if (it != null) {
                    val mediaItem = MediaItem(
                        mediaType = when(it.type) {
                            PlayInfo.TYPE_HLS -> MediaItem.MEDIA_TYPE_HLS
                            PlayInfo.TYPE_NORMAL -> MediaItem.MEDIA_TYPE_NORMAL
                            else -> MediaItem.MEDIA_TYPE_UNKNOWN
                        },
                        uri = it.url,
                        header = it.header
                    )
                    vlcjPlayerBridge.prepare(mediaItem)
                    vlcjPlayerBridge.setPlayWhenReady(true)
                    initializeVolumeStateIfNeeded()
                } else {
                    syncVolumeStateFromBridge()
                }
            }
        }
    }

    fun changeVolume(volume: Int) {
        updateVolume(volume, persist = true)
    }

    fun previewVolume(volume: Int) {
        if (updateVolume(volume, persist = false)) {
            schedulePersistVolume()
        }
    }

    fun commitPreviewVolume() {
        persistVolumeNow()
    }

    fun stepVolume(step: Int) {
        if (step == 0) {
            return
        }
        updateVolume(DesktopVolumeState.step(volume, step), persist = true)
    }

    fun toggleMute() {
        val action = volumeAction() ?: return
        if (!isVolumeControlAvailable) {
            return
        }
        if (!mute) {
            if (volume > 0) {
                lastVolumeBeforeMute = volume
            }
            if (setMuted(action, true)) {
                mute = true
            }
            return
        }

        val targetVolume = DesktopVolumeState.clamp(lastVolumeBeforeMute)
        if (setMuted(action, false)) {
            mute = false
            updateVolume(targetVolume, persist = true)
        }
    }

    fun onPlayInfoChange(playerInfoState: DataState<PlayInfo>) {
        playInfo.value = playerInfoState
    }

    override fun onCleared() {
        persistVolumeJob?.cancel()
        vlcjManager.release(this.toString())
        super.onCleared()
    }

    fun enterFullscreen() {
        fullscreenStrategy.enterFullscreen()
    }

    fun exitFullscreen() {
        fullscreenStrategy.exitFullscreen()
    }

    fun toggleFullscreen() {
        if (fullscreenStrategy.isFullscreen()) {
            fullscreenStrategy.exitFullscreen()
        } else {
            fullscreenStrategy.enterFullscreen()
        }
    }

    private fun initializeVolumeStateIfNeeded() {
        if (isVolumeInitialized) {
            syncVolumeStateFromBridge()
            return
        }

        val action = volumeAction() ?: return
        val preferredVolume = DesktopVolumeState.clamp(volumePref.get())
        val setResult = action.setVolume(preferredVolume)
        when (setResult) {
            is VolumeActionResult.Success -> {
                volume = DesktopVolumeState.clamp(setResult.value)
                if (volume > 0) {
                    lastVolumeBeforeMute = volume
                }
            }

            is VolumeActionResult.Failure -> {
                markVolumeControlUnavailable(setResult.reason)
                return
            }
        }

        when (val muteResult = action.setMuted(false)) {
            is VolumeActionResult.Success -> {
                mute = muteResult.value
            }

            is VolumeActionResult.Failure -> {
                markVolumeControlUnavailable(muteResult.reason)
                return
            }
        }

        isVolumeInitialized = true
        syncVolumeStateFromBridge()
    }

    private fun updateVolume(targetVolume: Int, persist: Boolean): Boolean {
        val action = volumeAction() ?: return false
        val clampedVolume = DesktopVolumeState.clamp(targetVolume)
        return when (val setResult = action.setVolume(clampedVolume)) {
            is VolumeActionResult.Success -> {
                val applied = DesktopVolumeState.clamp(setResult.value)
                markVolumeControlAvailable()
                volume = applied
                if (applied > 0) {
                    lastVolumeBeforeMute = applied
                }
                if (persist) {
                    volumePref.set(applied)
                }
                when (val muteResult = action.isMuted()) {
                    is VolumeActionResult.Success -> {
                        mute = muteResult.value
                        true
                    }

                    is VolumeActionResult.Failure -> {
                        markVolumeControlUnavailable(muteResult.reason)
                        false
                    }
                }
            }

            is VolumeActionResult.Failure -> {
                markVolumeControlUnavailable(setResult.reason)
                false
            }
        }
    }

    private fun syncVolumeStateFromBridge(): Boolean {
        val action = volumeAction() ?: return false
        val volumeResult = action.getVolume()
        val muteResult = action.isMuted()
        if (volumeResult is VolumeActionResult.Failure) {
            markVolumeControlUnavailable(volumeResult.reason)
            return false
        }
        if (muteResult is VolumeActionResult.Failure) {
            markVolumeControlUnavailable(muteResult.reason)
            return false
        }
        val synced = DesktopVolumeState.sync(
            volume = (volumeResult as VolumeActionResult.Success).value,
            muted = (muteResult as VolumeActionResult.Success).value,
        )
        volume = synced.volume
        mute = synced.muted
        if (synced.volume > 0) {
            lastVolumeBeforeMute = synced.volume
        }
        markVolumeControlAvailable()
        return true
    }

    private fun setMuted(action: VolumeAction<BaseVlcjPlayerBridge>, muted: Boolean): Boolean {
        return when (val setResult = action.setMuted(muted)) {
            is VolumeActionResult.Success -> {
                this.mute = setResult.value
                markVolumeControlAvailable()
                true
            }

            is VolumeActionResult.Failure -> {
                markVolumeControlUnavailable(setResult.reason)
                false
            }
        }
    }

    private fun schedulePersistVolume() {
        persistVolumeJob?.cancel()
        persistVolumeJob = viewModelScope.launch {
            delay(VOLUME_PERSIST_THROTTLE_MS.milliseconds)
            volumePref.set(DesktopVolumeState.clamp(volume))
            persistVolumeJob = null
        }
    }

    private fun persistVolumeNow() {
        persistVolumeJob?.cancel()
        persistVolumeJob = null
        volumePref.set(DesktopVolumeState.clamp(volume))
    }

    @Suppress("UNCHECKED_CAST")
    private fun volumeAction(): VolumeAction<BaseVlcjPlayerBridge>? {
        val action = vlcjPlayerBridge.action(VolumeAction::class)
        if (action == null) {
            markVolumeControlUnavailable(VolumeActionResult.Reason.UNSUPPORTED)
            return null
        }
        return action as VolumeAction<BaseVlcjPlayerBridge>
    }

    private fun markVolumeControlAvailable() {
        isVolumeControlAvailable = true
        volumeControlFailureReason = null
    }

    private fun markVolumeControlUnavailable(reason: VolumeActionResult.Reason) {
        isVolumeControlAvailable = false
        volumeControlFailureReason = reason
    }

}
