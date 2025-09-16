package org.easybangumi.next.shared.foundation

import android.app.Activity
import androidx.annotation.UiThread
import java.lang.ref.WeakReference

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
class ActivityController {

    private val activities = mutableListOf<WeakReference<Activity>>()
    private var lastResumeActivity : WeakReference<Activity>? = null

    @UiThread
    fun onCreate(activity: Activity) {
        activities.add(WeakReference(activity))
    }

    fun onResume(activity: Activity) {
        lastResumeActivity = WeakReference(activity)
    }

    @UiThread
    fun onDestroy(activity: Activity) {
        activities.removeAll { it.get() == activity || it.get() == null }
    }

    fun showingActivity(): Activity? {
        lastResumeActivity?.get()?.let {
            return it
        }
        activities.lastOrNull()?.get()?.let {
            return it
        }
        return null
    }

}