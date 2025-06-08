package org.easybangumi.next

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.easybangumi.next.shared.ActivityHost
import org.easybangumi.next.shared.ComposeApp
import org.easybangumi.next.shared.Scheduler

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // huawei crash
        setContentView(FrameLayout(this))
        Scheduler.onInit()
        setContent {
            ActivityHost(this@EasyActivity) {
                ComposeApp()
            }
        }
    }

}