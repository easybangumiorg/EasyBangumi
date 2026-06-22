package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.api.utils.core.AndroidCookieJar
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class JsonSourceRuleTest {

    @Test
    fun xpathSelectorExtractsTextAndAttributes() {
        val doc = XPathUtils.parse(
            """
            <html><body>
              <div class="item"><a href="/a/1">One</a></div>
              <div class="item"><a href="/a/2">Two</a></div>
            </body></html>
            """.trimIndent(),
            "https://example.test",
        )

        val items = XPathUtils.select(doc, "//div[contains(@class,'item')]")
        assertEquals(2, items.size)
        assertEquals(
            "/a/1",
            items.first()?.extract(
                SelectorRule(
                    query = ".//a",
                    type = SelectorType.XPATH,
                    attr = "href",
                )
            )
        )
    }

    @Test
    fun loaderAcceptsJsonSourceMetadata() {
        val file = File.createTempFile("json-source", ".json").apply {
            writeText(
                """
                {
                  "type": "easybangumi-json-source",
                  "key": "json.test",
                  "label": "Json Test",
                  "versionName": "1.0",
                  "versionCode": 1,
                  "libVersion": 15,
                  "site": { "baseUrl": "https://example.test" },
                  "pages": []
                }
                """.trimIndent()
            )
        }

        val loaded = JsonSourceFileLoader(file).load()
        assertTrue(loaded is com.heyanle.easybangumi4.plugin.source.SourceFileInfo.Loaded)
        assertEquals("json.test", loaded?.key)
        file.delete()
    }

    @Test
    fun loaderAcceptsConvertedAgeJsonSourceAndSearchesBocchi() {
        runBlocking {
            val file = File.createTempFile("age", ".json").apply {
                writeText(convertedAgeJsonSource)
            }

            val loaded = JsonSourceFileLoader(file).load()
            assertTrue(loaded is com.heyanle.easybangumi4.plugin.source.SourceFileInfo.Loaded)
            val source = (loaded as com.heyanle.easybangumi4.plugin.source.SourceFileInfo.Loaded).source as JsonSource
            assertEquals("AGE", source.key)
            assertEquals("https://www.agedm.io/", source.rule.site.baseUrl)
            assertEquals("https://www.agedm.io/search?query={keyword:url}", source.rule.search?.url)

            val executor = JsonRuleExecutor(
                source = source,
                networkHelper = FakeNetworkHelper,
                okhttpHelper = FakeOkhttpHelper,
                renderHelper = FakeRenderHelper,
                fetcher = { url ->
                    assertEquals(
                        "https://www.agedm.io/search?query=%E5%AD%A4%E7%8B%AC%E6%91%87%E6%BB%9A",
                        url,
                    )
                    """
                    <div></div>
                    <div>
                      <div>
                        <section>
                          <div>
                            <div>
                              <div>
                                <div>
                                  <div>
                                    <div></div>
                                    <div>
                                      <h5>
                                        <a href="/detail/20220127">&#23396;&#29420;&#25671;&#28378;&#65281;</a>
                                      </h5>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </section>
                      </div>
                    </div>
                    """.trimIndent()
                },
            )

            val (_, covers) = executor.loadList(source.rule.search!!, 1, "\u5b64\u72ec\u6447\u6eda")
            assertEquals(1, covers.size)
            assertEquals("\u5b64\u72ec\u6447\u6eda\uff01", covers.first().title)
            assertEquals("https://www.agedm.io/detail/20220127", covers.first().id)
            assertEquals("https://www.agedm.io/detail/20220127", covers.first().url)

            file.delete()
        }
    }

    @Test
    fun executorMapsListDetailAndDirectPlayToExistingEntities() = runBlocking {
        val rule = JsonSourceRule(
            key = "json.test",
            label = "Json Test",
            versionName = "1.0",
            versionCode = 1,
            libVersion = 15,
            site = SiteRule(baseUrl = "https://example.test"),
            pages = listOf(
                PageRule(
                    label = "Home",
                    list = ListRule(
                        url = "/page/{page}",
                        firstPage = 1,
                        item = SelectorRule(".item"),
                        fields = CoverFieldRule(
                            id = SelectorRule(".title", attr = "href"),
                            title = SelectorRule(".title"),
                            url = SelectorRule(".title", attr = "href"),
                            cover = SelectorRule("img", attr = "src"),
                            intro = SelectorRule(".intro"),
                        ),
                    ),
                )
            ),
            detail = DetailRule(
                url = "{url}",
                fields = CartoonFieldRule(
                    title = SelectorRule("h1"),
                    genre = SelectorRule(".genre"),
                    status = SelectorRule(".status"),
                ),
                playLines = PlayLineRule(
                    line = SelectorRule(".line"),
                    lineLabel = SelectorRule(".line-title"),
                    episode = SelectorRule("a.ep"),
                    episodeLabel = SelectorRule("", default = "ignored"),
                    episodeUrl = SelectorRule("", attr = "href"),
                ),
            ),
            play = PlayRule(
                url = "{url}",
                direct = SelectorRule("video", attr = "src"),
                renderVideo = false,
            ),
        )
        val source = JsonSource(rule, File("json.test.json"))
        val executor = JsonRuleExecutor(
            source = source,
            networkHelper = FakeNetworkHelper,
            okhttpHelper = FakeOkhttpHelper,
            renderHelper = FakeRenderHelper,
            fetcher = { url ->
                when {
                    url.endsWith("/page/1") -> """
                        <div class="item">
                          <a class="title" href="/detail/one">Title One</a>
                          <img src="/cover.jpg" />
                          <span class="intro">Intro One</span>
                        </div>
                    """.trimIndent()
                    url.endsWith("/detail/one") -> """
                        <h1>Title One Detail</h1>
                        <span class="genre">Action, TV</span>
                        <span class="status">完结</span>
                        <div class="line">
                          <b class="line-title">Main</b>
                          <a class="ep" href="/play/one-1">EP1</a>
                        </div>
                    """.trimIndent()
                    url.endsWith("/play/one-1") -> """<video src="https://cdn.example.test/one.m3u8"></video>"""
                    else -> error("unexpected url $url")
                }
            },
        )

        val (_, covers) = executor.loadList(rule.pages.first().list, 1)
        assertEquals(1, covers.size)
        assertEquals("Title One", covers.first().title)
        assertEquals("https://example.test/detail/one", covers.first().url)

        val (cartoon, lines) = executor.loadDetail(CartoonSummary(covers.first().url, source.key))
        assertEquals("Title One Detail", cartoon.title)
        assertEquals(Cartoon.STATUS_COMPLETED, cartoon.status)
        assertEquals("Main", lines.first().label)
        assertEquals("https://example.test/play/one-1", lines.first().episode.first().id)

        val playerInfo = executor.loadPlay(CartoonSummary(covers.first().url, source.key), lines.first(), lines.first().episode.first())
        assertEquals(PlayerInfo.DECODE_TYPE_HLS, playerInfo.decodeType)
        assertEquals("https://cdn.example.test/one.m3u8", playerInfo.uri)
    }

    private object FakeNetworkHelper : NetworkHelper {
        override val cookieManager: AndroidCookieJar
            get() = error("not used")
        override val defaultLinuxUA: String = "test-linux"
        override val defaultAndroidUA: String = "test-android"
        override val randomUA: String = "test-random"
    }

    private object FakeOkhttpHelper : OkhttpHelper {
        override val client: OkHttpClient = OkHttpClient()
        override val cloudflareClient: OkHttpClient = client
        override val cloudflareWebViewClient: OkHttpClient = client
    }

    private object FakeRenderHelper : RenderHelper {
        override suspend fun renderedHtml(strategy: RenderHelper.RenderedStrategy): RenderHelper.RenderedResult {
            error("not used")
        }

        override suspend fun renderVideo(strategy: RenderHelper.VideoStrategy): RenderHelper.VideoResult {
            return RenderHelper.VideoResult(strategy, "", true)
        }
    }

    private val convertedAgeJsonSource = """
        {
          "type": "easybangumi-json-source",
          "key": "AGE",
          "label": "AGE",
          "versionName": "1.5",
          "versionCode": 105,
          "libVersion": 15,
          "site": {
            "baseUrl": "https://www.agedm.io/"
          },
          "pages": [],
          "search": {
            "url": "https://www.agedm.io/search?query={keyword:url}",
            "firstPage": 1,
            "item": {
              "query": "//div[2]/div/section/div/div/div/div",
              "type": "XPATH"
            },
            "fields": {
              "id": {
                "query": "//div/div[2]/h5/a",
                "type": "XPATH",
                "attr": "href"
              },
              "title": {
                "query": "//div/div[2]/h5/a",
                "type": "XPATH"
              },
              "url": {
                "query": "//div/div[2]/h5/a",
                "type": "XPATH",
                "attr": "href"
              }
            }
          },
          "detail": {
            "url": "{url}",
            "playLines": {
              "line": {
                "query": "//div[2]/div/section/div/div[2]/div[2]/div[2]/div",
                "type": "XPATH"
              },
              "lineLabel": {
                "query": "",
                "default": "Default"
              },
              "episode": {
                "query": "//ul/li/a",
                "type": "XPATH"
              },
              "episodeLabel": {
                "query": ""
              },
              "episodeUrl": {
                "query": "",
                "attr": "href"
              }
            }
          },
          "play": {
            "url": "{url}",
            "renderVideo": true,
            "useLegacyParser": false
          }
        }
    """.trimIndent()
}
