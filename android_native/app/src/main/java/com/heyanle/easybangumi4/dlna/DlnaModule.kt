package com.heyanle.easybangumi4.dlna

import android.app.Application
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get
import org.cybergarage.upnp.ControlPoint

/**
 * Created by heyanlin on 2024/2/6 15:27.
 */
class DlnaModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            ControlPoint()
        }
        addSingletonFactory {
            EasyDlna(get())
        }
    }
}