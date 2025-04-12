package org.easybangumi.next.shared.plugin.component

import org.easybangumi.next.shared.plugin.source.Source


/**
 * Created by heyanlin on 2024/12/9.
 */
open class ComponentWrapper : Component {

    var innerSource: Source? = null

    override val source: Source
        get() = innerSource!!

}