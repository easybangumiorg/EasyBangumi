package org.easybangumi.next

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import org.easybangumi.next.shared.ActivityHost
import org.easybangumi.next.shared.ComposeApp
import org.easybangumi.next.shared.Scheduler
import org.easybangumi.next.shared.foundation.ActivityController
import org.easybangumi.next.shared.foundation.utils.MediaUtils
import org.koin.android.ext.android.inject

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
class EasyActivity : ComponentActivity() {

    val activityController: ActivityController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // huawei crash
        setContentView(FrameLayout(this))
        Scheduler.onInit()
        activityController.onCreate(this)
        MediaUtils.setIsDecorFitsSystemWindows(this, false)
        MediaUtils.setStatusBarColor(this, Color.TRANSPARENT)
        MediaUtils.setNavBarColor(this, Color.TRANSPARENT)
//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.statusBarColor = Color.TRANSPARENT
        setContent {
            ActivityHost(this@EasyActivity) {
                ComposeApp()
            }
        }
    }

    override fun onDestroy() {
        activityController.onDestroy(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        activityController.onResume(this)
    }
}