package com.heyanle.easybangumi4.base

import com.heyanle.easybangumi4.plugin.api.SourceResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataResultTest {

    @Test
    fun cacheProvenanceSurvivesConversionsAndMapping() {
        val converted = SourceResult.Complete("cached", isCache = true).toDataResult()
        val mapped = converted.map(String::length)

        assertTrue(converted.isCache)
        assertTrue(mapped.isCache)
        assertFalse(DataResult.ok("network").isCache)
        assertFalse(DataResult.Loading<String>().isCache)
        assertFalse(DataResult.error<String>("failed").isCache)
    }
}
