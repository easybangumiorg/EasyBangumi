package com.heyanle.easybangumi4

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.v2.MainV2Activity
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject

/**
 * Resolves the selected main UI and owns the task-safe switch between both versions.
 *
 * Keeping the target selection here prevents launcher, settings and notification entry points
 * from drifting apart while both activities coexist.
 */
object MainActivitySwitcher {

    fun createMainIntent(context: Context): Intent {
        val preferences = Inject.get<SettingPreferences>()
        return createMainIntent(context, preferences.useV2Ui.get())
    }

    fun createMainIntent(context: Context, useV2Ui: Boolean): Intent {
        return Intent(context, resolveTargetActivity(useV2Ui))
    }

    internal fun resolveTargetActivity(useV2Ui: Boolean) =
        if (useV2Ui) MainV2Activity::class.java else MainActivity::class.java

    fun switch(context: Context, useV2Ui: Boolean) {
        val preferences = Inject.get<SettingPreferences>()
        preferences.useV2Ui.set(useV2Ui)

        val targetIntent = createMainIntent(context, useV2Ui)
        val restartIntent = Intent.makeRestartActivityTask(
            ComponentName(context, requireNotNull(targetIntent.component?.className)),
        )
        context.startActivity(restartIntent)
    }
}
