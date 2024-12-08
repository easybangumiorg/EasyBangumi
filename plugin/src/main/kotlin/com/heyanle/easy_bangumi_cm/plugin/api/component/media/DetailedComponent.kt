package com.heyanle.easy_bangumi_cm.plugin.api.component.media

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentContainer
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent
import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonDetailed
import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonIndex
import com.heyanle.easy_bangumi_cm.repository.play.PlayerLine


/**
 * Created by HeYanLe on 2024/12/8 22:04.
 * https://github.com/heyanLE
 */

interface DetailedComponent : MediaComponent {

    suspend fun detailed(cartoonIndex: CartoonIndex): SourceResult<Pair<CartoonDetailed, List<PlayerLine>>>

}

fun ComponentContainer.detailedComponent(): DetailedComponent?{
    return this.getComponent(DetailedComponent::class)
}