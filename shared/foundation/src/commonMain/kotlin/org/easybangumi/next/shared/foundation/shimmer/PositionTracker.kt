package org.easybangumi.next.shared.foundation.shimmer

import androidx.compose.ui.geometry.Offset

class PositionTracker {
    private val positions = mutableMapOf<String, Offset>()

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
}