package com.heyanle.easybangumi4.dlna

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import org.cybergarage.upnp.ControlPoint

/**
 * Created by heyanlin on 2024/2/6 15:27.
 */
class DlnaModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            ControlPoint()
        }
        addSingletonFactory {
            EasyDlna(get())
        }
    }
}