package com.heyanle.easy_bangumi_cm.plugin.api.component

import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
interface EventComponent: Component {

    fun onLoad(sourceManifest: SourceManifest){}

    fun onUnload(sourceManifest: SourceManifest){}

}