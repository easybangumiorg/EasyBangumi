package org.easybangumi.next.shared.bangumi.data.repository

import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.room.cartoon.dao.CartoonInfoDao

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
class BangumiInfoRepository(
    private val cartoonIndex: CartoonIndex,
    private val subjectRepository: DataRepository<BgmSubject>,
    private val collection: DataRepository<BgmCollect>,
    cartoonInfoDao: CartoonInfoDao,
) {
}