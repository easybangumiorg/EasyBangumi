package com.heyanle.easybangumi4.player.exo

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.setting.SettingPreferences
import io.github.fongmi.adaudio.probe.AdAudioProbe
import io.github.fongmi.adaudio.probe.ProbeMedia
import io.github.fongmi.adaudio.probe.adapter.media3.v1_9.Media3ProbeAdapterFactory
import okio.ByteString.Companion.encodeUtf8

internal fun stableProbeMediaId(rawMediaId: String): String =
    "easybangumi-${rawMediaId.encodeUtf8().sha256().hex()}"

/**
 * ExoPlayer-only bridge for m3u8-ad-audio-probe.
 *
 * The probe is fail-open: it owns a separate audio-only decoder and can only ask this controller
 * to seek after a rule confirms an ad. mpv deliberately never creates this controller.
 */
class ExoAdAudioProbeController(
    context: Context,
    private val player: ExoPlayer,
    private val preferences: SettingPreferences,
) {
    private val appContext = context.applicationContext
    private var probe: AdAudioProbe? = null
    private var configuredRulesUrl: String? = null

    fun open(playerInfo: PlayerInfo, mediaId: String) {
        if (!preferences.exoAdAudioProbeEnabled.get()) {
            probe?.setEnabled(false)
            return
        }
        val mediaType = when (playerInfo.decodeType) {
            C.CONTENT_TYPE_HLS -> ProbeMedia.Type.HLS
            C.CONTENT_TYPE_OTHER -> ProbeMedia.Type.MP4
            else -> return
        }
        val currentProbe = ensureProbe() ?: return
        val media = runCatching {
            ProbeMedia.builder(playerInfo.uri)
                .setId(stableProbeMediaId(mediaId))
                .setType(mediaType)
                .apply {
                    for ((name, value) in playerInfo.header.orEmpty()) {
                        if (isSupportedHeader(name)) {
                            setHeader(name, value)
                        }
                    }
                }
                .build()
        }.onFailure {
            Log.w(TAG, "Skip invalid ad-audio probe media", it)
        }.getOrNull() ?: return
        runCatching {
            currentProbe.open(media)
        }.onFailure {
            Log.w(TAG, "Ad-audio probe open failed", it)
        }
    }

    fun notifyHostDiscontinuity() {
        probe?.notifyHostDiscontinuity(player.currentPosition.coerceAtLeast(0L))
    }

    fun refreshConfiguration() {
        if (!preferences.exoAdAudioProbeEnabled.get()) {
            probe?.setEnabled(false)
            return
        }
        if (configuredRulesUrl != normalizedRulesUrl()) {
            probe?.close()
            probe = null
            configuredRulesUrl = null
        }
    }

    fun close() {
        probe?.close()
        probe = null
        configuredRulesUrl = null
    }

    private fun ensureProbe(): AdAudioProbe? {
        val rulesUrl = normalizedRulesUrl()
        val existing = probe
        if (existing != null && configuredRulesUrl == rulesUrl) {
            existing.setEnabled(true)
            return existing
        }
        existing?.close()
        return runCatching {
            val builder = if (rulesUrl.isEmpty()) {
                AdAudioProbe.builder(appContext)
                    .setInitialRulesJson(EMPTY_RULES_JSON)
            } else {
                AdAudioProbe.builder(appContext, rulesUrl)
            }
            builder
                .setPlaybackClock { player.currentPosition.coerceAtLeast(0L) }
                .setAdapterFactory(Media3ProbeAdapterFactory())
                .setListener { request ->
                    player.seekTo(request.seekTargetPositionMs)
                }
                .build()
                .also {
                    probe = it
                    configuredRulesUrl = rulesUrl
                }
        }.getOrNull()
    }

    private fun normalizedRulesUrl(): String = preferences.exoAdAudioProbeRulesUrl.get().trim()

    private fun isSupportedHeader(name: String): Boolean = name.equals("User-Agent", true) ||
        name.equals("Accept", true) ||
        name.equals("Accept-Language", true) ||
        name.equals("Cache-Control", true) ||
        name.equals("Pragma", true)

    private companion object {
        const val TAG = "ExoAdAudioProbe"
        const val EMPTY_RULES_JSON = """
            {"format":"ad-audio-probe-rules","schemaVersion":1,"revision":1,"algorithm":"spectral-sequence-v1","rules":[]}
        """
    }
}
