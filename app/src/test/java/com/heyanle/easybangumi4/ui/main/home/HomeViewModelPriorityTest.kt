package com.heyanle.easybangumi4.ui.main.home

import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.reflect.KClass

class HomeViewModelPriorityTest {

    @Test
    fun giriGiriLoveIsMovedFirstAndOtherSourcesKeepTheirOrder() {
        val pages = listOf(page("source-a"), page("heyanle.ggl"), page("source-b"))

        assertEquals(
            listOf("heyanle.ggl", "source-a", "source-b"),
            HomeViewModel.prioritizeHomeSources(pages).map { it.source.key },
        )
    }

    @Test
    fun sourceOrderIsUnchangedWhenGiriGiriLoveIsMissing() {
        val pages = listOf(page("source-a"), page("source-b"))

        assertEquals(
            listOf("source-a", "source-b"),
            HomeViewModel.prioritizeHomeSources(pages).map { it.source.key },
        )
    }

    private fun page(key: String): PageComponent = object : PageComponent {
        override val source: Source = object : Source {
            override val key = key
            override val label = key
            override val version = "1.0"
            override val versionCode = 1
            override val describe: String? = null
            override fun register(): List<KClass<*>> = emptyList()
        }

        override fun getPages(): List<SourcePage> = emptyList()
    }
}
