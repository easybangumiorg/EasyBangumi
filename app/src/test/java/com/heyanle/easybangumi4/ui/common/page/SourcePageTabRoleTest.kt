package com.heyanle.easybangumi4.ui.common.page

import org.junit.Assert.assertEquals
import org.junit.Test

class SourcePageTabRoleTest {
    @Test fun realParentKeepsChildrenSecondary() {
        assertEquals(SourcePageTabRole.Secondary, sourcePageTabRole(true, 3))
    }
    @Test fun explicitlyOmittedParentPromotesChildren() {
        assertEquals(SourcePageTabRole.Primary, sourcePageTabRole(false, 3))
    }
    @Test fun singleChildDoesNotCreatePlaceholderNavigation() {
        assertEquals(SourcePageTabRole.Hidden, sourcePageTabRole(false, 1))
        assertEquals(SourcePageTabRole.Hidden, sourcePageTabRole(true, 1))
    }
    @Test fun emptyGroupDoesNotCreateNavigation() {
        assertEquals(SourcePageTabRole.Hidden, sourcePageTabRole(false, 0))
        assertEquals(SourcePageTabRole.Hidden, sourcePageTabRole(true, 0))
    }
}
