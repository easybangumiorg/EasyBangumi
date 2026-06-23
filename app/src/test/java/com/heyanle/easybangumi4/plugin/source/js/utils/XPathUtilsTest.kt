package com.heyanle.easybangumi4.plugin.source.js.utils

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class XPathUtilsTest {

    @Test
    fun selectsNodesAndAttributesWithJsoupXpath() {
        val doc = Jsoup.parse(
            """
            <html>
              <body>
                <div class="result">
                  <h5><a href="/detail/one">One</a></h5>
                </div>
                <div class="result">
                  <h5><a href="/detail/two">Two</a></h5>
                </div>
              </body>
            </html>
            """.trimIndent()
        )

        val items = XPathUtils.nodes(doc, "//div[@class='result']")

        assertEquals(2, items.size)
        assertEquals("One", XPathUtils.text(items[0], "//h5/a"))
        assertEquals("/detail/two", XPathUtils.attr(items[1], "//h5/a", "href"))
    }
}
