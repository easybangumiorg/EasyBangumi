package org.easybangumi.next.shared.plugin.api.component

import org.easybangumi.next.shared.plugin.source.SourceManifest


/**
 * Created by heyanlin on 2024/12/13.
 */
interface EventComponent: Component {

    fun onLoad(sourceManifest: SourceManifest){}

    fun onUnload(sourceManifest: SourceManifest){}

}