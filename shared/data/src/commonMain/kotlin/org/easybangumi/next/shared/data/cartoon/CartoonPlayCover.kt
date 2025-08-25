package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient


/**
 * Created by HeYanLe on 2025/8/24 15:10.
 * https://github.com/heyanLE
 */

class CartoonPlayCover(
    val id: String,
    val source: String,

    val name: String,
    val coverUrl: String,

    val webUrl: String,
    val tags: List<String>,
    val desc: String,

): Extractor {

    @kotlinx.serialization.Transient
    @Transient
    override var ext: String = ""

}