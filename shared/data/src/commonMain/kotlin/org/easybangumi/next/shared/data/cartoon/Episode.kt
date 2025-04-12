package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient

/**
 * Created by heyanlin on 2024/12/5.
 */
class Episode (
    val id: String,
    val label: String,
    val order: Int,
    val playInfo: PlayInfo,
): Extractor {

    @Transient
    override var ext: String = ""
}