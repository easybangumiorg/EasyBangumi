package org.easybangumi.next.shared.plugin.api.component.pref

import org.easybangumi.next.shared.plugin.api.component.Component


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

interface PrefComponent : Component {

    suspend fun register(): List<SourcePreference>

}