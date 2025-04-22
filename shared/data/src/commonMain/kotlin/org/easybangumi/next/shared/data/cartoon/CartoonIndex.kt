package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient

/**
 * Created by heyanlin on 2024/12/5.
 */
class CartoonIndex(
    val id: String,
    val fromSource: String,
): Extractor {

    @Transient
    override var ext: String = ""


    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fromSource.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonIndex

        if (id != other.id) return false
        if (fromSource != other.fromSource) return false
        if (ext != other.ext) return false

        return true
    }


}