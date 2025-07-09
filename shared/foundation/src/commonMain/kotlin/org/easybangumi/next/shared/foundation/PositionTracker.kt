package org.easybangumi.next.shared.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize

class PositionTracker {

    private val positions = mutableMapOf<String, Offset>()
    private val size = mutableMapOf<String, IntSize>()

    fun setPosition(key: String, position: Offset) {
        positions[key] = position
    }

    fun getPosition(key: String): Offset? = positions[key]

    fun getRelativePosition(childKey: String, parentKey: String): Offset? {
        val childPos = positions[childKey]
        val parentPos = positions[parentKey]
        return if (childPos != null && parentPos != null) {
            childPos - parentPos
        } else {
            null
        }
    }

    fun setSize(key: String, size: IntSize) {
        this.size[key] = size
    }

    fun getSize(key: String): IntSize? = size[key]

}

