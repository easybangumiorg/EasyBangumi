package org.easybangumi.next.shared.plugin.api.component

import org.easybangumi.next.shared.plugin.api.source.Source
import org.koin.core.component.KoinComponent


/**
 * Created by HeYanLe on 2024/12/8 21:10.
 * https://github.com/heyanLE
 */

interface Component: KoinComponent {

    val source: Source

}