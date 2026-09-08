package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.DownloadDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartoonDownloadReqModelStateTest {

    @Test
    fun downloadDefaultsToLocalCache() {
        val state = CartoonDownloadReqModel.State()

        assertEquals(DownloadDestination.FLAT, state.destination)
        assertTrue(state.isFlat)
    }
}
