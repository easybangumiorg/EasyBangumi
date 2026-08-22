package com.heyanle.easybangumi4.v2.navigation

import com.heyanle.easybangumi4.ABOUT
import com.heyanle.easybangumi4.CARTOON_MIGRATE
import com.heyanle.easybangumi4.DETAILED
import com.heyanle.easybangumi4.DETAILED_LEGACY
import com.heyanle.easybangumi4.DLNA
import com.heyanle.easybangumi4.HISTORY
import com.heyanle.easybangumi4.LOCAL_PLAY
import com.heyanle.easybangumi4.MAIN
import com.heyanle.easybangumi4.SEARCH
import com.heyanle.easybangumi4.SETTING
import com.heyanle.easybangumi4.SOURCE_CONFIG
import com.heyanle.easybangumi4.SOURCE_MANAGER
import com.heyanle.easybangumi4.SOURCE_PUSH
import com.heyanle.easybangumi4.STORAGE
import com.heyanle.easybangumi4.STORY
import com.heyanle.easybangumi4.TAG_MANAGE
import com.heyanle.easybangumi4.WEB_VIEW_USER
import org.junit.Assert.assertEquals
import org.junit.Test

class V2RouteContractTest {

    @Test
    fun v2ShellMirrorsEveryRegisteredLegacyDestination() {
        val legacyDestinationBases = setOf(
            MAIN,
            DETAILED,
            DETAILED_LEGACY,
            DLNA,
            LOCAL_PLAY,
            SETTING,
            WEB_VIEW_USER,
            HISTORY,
            STORY,
            SOURCE_PUSH,
            SOURCE_MANAGER,
            SEARCH,
            CARTOON_MIGRATE,
            ABOUT,
            SOURCE_CONFIG,
            TAG_MANAGE,
            STORAGE,
        )

        assertEquals(17, V2Routes.allDestinationBases.size)
        assertEquals(legacyDestinationBases, V2Routes.allDestinationBases)
    }
}
