package com.heyanle.easybangumi4.splash

import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon.local.CartoonLocalController
import com.heyanle.easybangumi4.splash.step.LocalStep
import com.heyanle.easybangumi4.splash.step.PermissionStep
import com.heyanle.easybangumi4.splash.step.ThemeStep
import com.heyanle.okkv2.core.okkv

/**
 * Created by heyanlin on 2024/7/4.
 */
class SplashGuildController(
    private val androidPreferenceStore: AndroidPreferenceStore,
    private val localController: CartoonLocalController,
) {


    private val stepList = listOf(
        ThemeStep(),
        LocalStep(),
        PermissionStep(),
    )
    val realStep = stepList.flatMap {
        val version by okkv("splash_step_${it.name}_version", def = -1)
        if(it.version > version){
            listOf(it)
        } else {
            emptyList()
        }
    }





}