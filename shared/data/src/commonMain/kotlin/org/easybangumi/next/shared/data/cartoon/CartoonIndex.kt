package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

/**
 * Created by heyanle on 2024/12/5.
 */
@Serializable
data class CartoonIndex(
    val id: String,
    val source: String,
): Extractor {

//    @Transient
    override var ext: String = ""


    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonIndex

        if (id != other.id) return false
        if (source != other.source) return false
//        if (ext != other.ext) return false

        return true
    }


}