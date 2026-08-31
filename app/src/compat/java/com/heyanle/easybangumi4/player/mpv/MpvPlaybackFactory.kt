package com.heyanle.easybangumi4.player.mpv

import android.content.Context
import com.heyanle.easybangumi4.setting.SettingPreferences

/** The normal APK deliberately has no mpv implementation or native dependency. */
internal fun createFlavorMpvPlaybackController(
    context: Context,
    preferences: SettingPreferences,
): MpvPlaybackControllerContract? = null
