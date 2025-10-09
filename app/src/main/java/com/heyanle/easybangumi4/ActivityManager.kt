package com.heyanle.easybangumi4

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Created by heyanlin on 2025/10/9.
 */
object ActivityManager {

    val activityList = mutableListOf<WeakReference<Activity>>()

    fun addActivity(activity: Activity){
        activityList.add(WeakReference(activity))
    }

    fun removeActivity(activity: Activity){
        activityList.firstOrNull { it.get() == activity }?.let {
            activityList.remove(it)
        }
    }

    fun finishAll(){
        activityList.forEach {
            it.get()?.finish()
        }
        activityList.clear()
    }

    fun getTopActivity(): Activity?{
        return activityList.lastOrNull()?.get()
    }

}