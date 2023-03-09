package com.heyanle.easybangumi4.ui.common.player.utils

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.utils.loge
import kotlin.math.roundToInt

/**
 * Created by LoliBall on 2023/3/2 16:22.
 * https://github.com/WhichWho
 */

var systemVolume: Float
    get() {
        val mAudioManager = APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return mCurrentVolume.toFloat() / mMaxVolume
    }
    set(value) {
        "set volume $value".loge("volume")
        val mAudioManager = APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val coerceValue = value.coerceIn(0f, 1f)
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (coerceValue * mMaxVolume).roundToInt(),
            AudioManager.FLAG_SHOW_UI
        )
    }


var Activity.windowBrightness: Float
    get() = window.windowBrightness
    set(value) {
        window.windowBrightness = value
    }

var Window.windowBrightness: Float
    get() {
        val brightness = attributes.screenBrightness
        if (brightness != BRIGHTNESS_OVERRIDE_NONE) return brightness
        else return screenBrightness
    }
    set(value) {
        "set brightness $value".loge("brightness")
        val coerceValue = value.coerceIn(0f, 1f)
        attributes = attributes.also {
            it.screenBrightness = coerceValue
        }
    }

var Activity.isKeepScreenOn: Boolean
    get() = window.isKeepScreenOn
    set(value) {
        window.isKeepScreenOn = value
    }

var Window.isKeepScreenOn: Boolean
    get() = attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0
    set(value) {
        if (value) addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

val screenBrightness: Float
    get() {
        val contentResolver = APP.contentResolver
        val raw = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 125)
        return raw / 255f
    }
