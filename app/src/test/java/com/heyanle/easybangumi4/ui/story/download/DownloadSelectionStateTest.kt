package com.heyanle.easybangumi4.ui.story.download

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadSelectionStateTest {

    @Test
    fun `runtime replacement keeps selected task id`() {
        assertEquals(
            setOf("task-1"),
            retainValidTaskSelection(
                selectionIds = setOf("task-1"),
                latestTaskIds = setOf("task-1", "task-2"),
            ),
        )
    }

    @Test
    fun `completed or removed tasks are pruned from selection`() {
        assertEquals(
            setOf("task-2"),
            retainValidTaskSelection(
                selectionIds = setOf("task-1", "task-2"),
                latestTaskIds = setOf("task-2", "task-3"),
            ),
        )
    }
}
