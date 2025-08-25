package org.easybangumi.next.shared.source.api.component.pref

import org.easybangumi.next.shared.source.api.component.Component


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

interface IPrefComponent {

    suspend fun register(): List<MediaSourcePreference>

}

interface PrefComponent: Component, IPrefComponent