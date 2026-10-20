package com.heyanle.easybangumi4.plugin.source.js.component

import com.heyanle.easybangumi4.plugin.source.js.entity.MainTab
import com.heyanle.easybangumi4.plugin.source.js.entity.NonLabelMainTab
import org.junit.Assert.*
import org.junit.Test
import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.Wrapper
import java.io.File

class JSPageTabsTest {
    @Test fun migratedHomeScriptsReturnTheJavaNoLabelMarker() {
        val files = File("../inner_source").listFiles().orEmpty().filter {
            it.extension == "js" && it.readText().contains("return new NonLabelMainTab(")
        }
        assertTrue("Expected migrated home sources", files.isNotEmpty())
        Context.enter().apply { optimizationLevel = -1 }.use { context ->
            val scope = ImporterTopLevel(context)
            context.evaluateString(scope, """
                importClass(Packages.com.heyanle.easybangumi4.plugin.source.js.entity.MainTab);
                importClass(Packages.com.heyanle.easybangumi4.plugin.source.js.entity.NonLabelMainTab);
            """.trimIndent(), "imports", 1, null)
            files.forEach { file ->
                val function = Regex("""function PageComponent_getMainTabs\(\)\s*\{[\s\S]*?\n}""")
                    .find(file.readText())!!.value
                val result = context.evaluateString(scope, function + "\nPageComponent_getMainTabs();", file.name, 1, null)
                val marker = (result as Wrapper).unwrap()
                assertTrue(file.name, marker is NonLabelMainTab)
                assertEquals(MainTab.MAIN_TAB_WITH_COVER, normalizeMainTabs(marker as NonLabelMainTab).single().type)
            }
        }
    }

    @Test fun noLabelGroupPreservesOpaqueSourceParameters() {
        val ext = java.lang.Object()
        val tab = normalizeMainTabs(NonLabelMainTab(MainTab.MAIN_TAB_GROUP, ext)).single()
        assertEquals("", tab.label)
        assertEquals(MainTab.MAIN_TAB_GROUP, tab.type)
        assertSame(ext, tab.ext)
    }
    @Test fun noLabelSinglePageRetainsItsPageType() {
        val tab = normalizeMainTabs(NonLabelMainTab(MainTab.MAIN_TAB_WITH_COVER)).single()
        assertEquals("", tab.label)
        assertEquals(MainTab.MAIN_TAB_WITH_COVER, tab.type)
    }
    @Test fun ordinarySingleTabKeepsItsLabel() {
        val original = MainTab("日本动漫", MainTab.MAIN_TAB_WITH_COVER)
        assertSame(original, normalizeMainTabs(arrayListOf(original)).single())
    }
}
