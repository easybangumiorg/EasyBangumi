package com.heyanle.easybangumi4.ui.common.page

/** Navigation role after accounting for an explicitly omitted parent tab row. */
enum class SourcePageTabRole { Primary, Secondary, Hidden }

internal fun sourcePageTabRole(hasParentTabs: Boolean, pageCount: Int): SourcePageTabRole = when {
    pageCount <= 1 -> SourcePageTabRole.Hidden
    hasParentTabs -> SourcePageTabRole.Secondary
    else -> SourcePageTabRole.Primary
}
