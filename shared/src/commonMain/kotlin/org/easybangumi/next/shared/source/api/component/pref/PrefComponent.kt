package org.easybangumi.next.shared.source.api.component.pref

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.component.Component


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

interface IPrefComponent {

    suspend fun register(): DataState<List<MediaSourcePreference>>

}

interface InnerPrefComponent: Component,IPrefComponent {
    fun registerInner(): List<MediaSourcePreference>

    override suspend fun register(): DataState<List<MediaSourcePreference>> {
        return DataState.Ok(registerInner())
    }
}
interface PrefComponent: Component, IPrefComponent
