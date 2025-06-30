package org.easybangumi.shared.plugin.bangumi.business

import kotlinx.coroutines.Deferred
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.CalendarItem
import org.easybangumi.shared.plugin.bangumi.model.Subject

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

interface BangumiApi {
    // get subjects/{subjectId}
    fun getSubject(subjectId: String): Deferred<BgmRsp<Subject>>

    // get calendar
    fun calendar(): Deferred<BgmRsp<List<CalendarItem>>>

    // get bangumi.proxy/trends?page={page}
    fun getTrends(page: Int): Deferred<BgmRsp<List<Subject>>>
}