package org.easybangumi.next.shared.case

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.shared.bangumi.data.BangumiDataController
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.bangumi.BgmCharacter
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.bangumi.BgmPerson
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.source.case.DetailSourceCase

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
class BangumiCase(
    private val bangumiDataController: BangumiDataController,
    private val detailSourceCase: DetailSourceCase,
) {
    private val logger = logger()

    fun getSubjectRepository(cartoonIndex: CartoonIndex): DataRepository<BgmSubject> {
        return bangumiDataController.getSubjectRepository(cartoonIndex).apply {
            logger.info("getSubjectRepository: $cartoonIndex, repository: $this" )
        }
    }

    fun getPersonListRepository(cartoonIndex: CartoonIndex): DataRepository<List<BgmPerson>> {
        return bangumiDataController.getPersonListRepository(cartoonIndex)
    }

    fun getCharacterListRepository(cartoonIndex: CartoonIndex): DataRepository<List<BgmCharacter>> {
        return bangumiDataController.getCharacterListRepository(cartoonIndex)
    }

    fun getCollectRepository(cartoonIndex: CartoonIndex): DataRepository<BgmCollect>? {
        return bangumiDataController.getCollectRepository(cartoonIndex)
    }

    fun coverUrl(cartoonIndex: CartoonIndex): String{
        return detailSourceCase.getBangumiDetailBusiness().runDirect {
            coverUrl(cartoonIndex)
        }
    }
}