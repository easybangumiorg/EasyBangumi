package com.heyanle.easybangumi4.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon_local.CartoonLocalController
import com.heyanle.easybangumi4.splash.step.BaseStep
import com.heyanle.easybangumi4.splash.step.LocalStep
import com.heyanle.easybangumi4.splash.step.PermissionStep
import com.heyanle.easybangumi4.splash.step.ThemeStep
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

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