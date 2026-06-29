package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

/**
 * Created by heyanle on 2024/12/5.
 */
@Serializable
class Episode (
    val id: String,
    val label: String,
    val order: Int,
): Extractor {

    @kotlinx.serialization.Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Episode

        if (order != other.order) return false
        if (id != other.id) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + id.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }


}