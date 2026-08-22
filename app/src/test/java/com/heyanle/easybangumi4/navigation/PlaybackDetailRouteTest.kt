package com.heyanle.easybangumi4.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLDecoder

class PlaybackDetailRouteTest {

    @Test
    fun defaultPlaybackRouteTargetsV2() {
        val route = buildPlaybackDetailRoute(
            id = "cartoon-1",
            source = "source-1",
        )

        assertEquals(PlaybackDetailTarget.V2, DEFAULT_PLAYBACK_DETAIL_TARGET)
        assertEquals(PLAYBACK_DETAIL_V2_ROUTE, route.substringBefore('?'))
        assertEquals("{}", decodeRoute(route).enterDataJson)
    }

    @Test
    fun explicitLegacyRouteTargetsPreservedPage() {
        val route = buildPlaybackDetailRoute(
            id = "cartoon-1",
            source = "source-1",
            target = PlaybackDetailTarget.Legacy,
        )

        assertEquals(PLAYBACK_DETAIL_LEGACY_ROUTE, route.substringBefore('?'))
    }

    @Test
    fun v2AndLegacyPreserveTheSamePlaybackArguments() {
        val expected = PlaybackDetailRouteArguments(
            id = "番剧/id?第一季",
            source = "inner/source & 测试",
            enterDataJson = """{"playLineId":"线路+1","episodeId":"ep/12?","adviceProgress":12345}""",
        )
        val v2 = buildPlaybackDetailRoute(
            id = expected.id,
            source = expected.source,
            enterDataJson = expected.enterDataJson,
            target = PlaybackDetailTarget.V2,
        )
        val legacy = buildPlaybackDetailRoute(
            id = expected.id,
            source = expected.source,
            enterDataJson = expected.enterDataJson,
            target = PlaybackDetailTarget.Legacy,
        )

        assertEquals(expected, decodeRoute(v2))
        assertEquals(expected, decodeRoute(legacy))
    }

    private fun decodeRoute(route: String): PlaybackDetailRouteArguments {
        val query = route.substringAfter('?')
            .split('&')
            .associate { item ->
                val pair = item.split('=', limit = 2)
                pair.first() to pair.getOrElse(1) { "" }
            }
        return decodePlaybackDetailRouteArguments(
            id = decodeNavigationArgument(query.getValue("id")),
            source = decodeNavigationArgument(query.getValue("source")),
            enterDataJson = decodeNavigationArgument(query.getValue("enter_data")),
        )
    }

    /** 模拟 Navigation 匹配 URI 后交给 BackStackEntry 的已解码字符串。 */
    private fun decodeNavigationArgument(value: String): String =
        URLDecoder.decode(value, Charsets.UTF_8.name())
}
