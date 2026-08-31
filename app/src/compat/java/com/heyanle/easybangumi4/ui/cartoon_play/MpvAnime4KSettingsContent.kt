package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.ui.Modifier
import com.heyanle.easybangumi4.player.mpv.MpvAnime4KStatus
import com.heyanle.easybangumi4.setting.SettingPreferences

/** Normal builds have no mpv/Anime4K settings surface. */
internal fun MpvAnime4KSettingsContent(
    enabled: Boolean,
    preset: SettingPreferences.MpvAnime4KPreset,
    status: MpvAnime4KStatus,
    onEnabledChange: (Boolean) -> Unit,
    onPresetChange: (SettingPreferences.MpvAnime4KPreset) -> Unit,
    modifier: Modifier = Modifier,
) = Unit
