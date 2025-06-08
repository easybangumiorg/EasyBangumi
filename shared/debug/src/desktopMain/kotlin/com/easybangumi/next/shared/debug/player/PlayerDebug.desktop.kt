package com.easybangumi.next.shared.debug.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.easybangumi.next.shared.debug.DebugScope
import org.easybangumi.next.player.api.MediaItem
import org.easybangumi.next.player.vlcj.VlcjPlayerBridge
import org.easybangumi.next.player.vlcj.VlcjPlayerFrame
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform
import uk.co.caprica.vlcj.factory.MediaPlayerFactory

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

val url = "http://vjs.zencdn.net/v/oceans.mp4"

@Composable
actual fun DebugScope.PlayerDebug() {

    val bridge = remember {
        VlcjPlayerBridge(KoinPlatform.getKoin().get(), null).apply {
            prepare(MediaItem(uri = url))
            setPlayWhenReady(true)
        }

    }

    VlcjPlayerFrame(
        modifier = Modifier.fillMaxSize(),
        bridge = bridge,
    )


}