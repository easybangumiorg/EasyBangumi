package org.easybangumi.next.shared.bangumi.data

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.CoroutineProvider
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCollectionRepository
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.case.DetailSourceCase

/**
 * bangumi 登录态有关数据
 * Created by heyanlin on 2025/11/5.
 */
class BangumiUserDataProvider(
    private val info: BangumiAccountController.BangumiAccountInfo,
    private val bangumiRootFileUfd: UFD,
    private val detailSourceCase: DetailSourceCase,
    private val lock: ReentrantLock,
    private val scope: CoroutineScope,
) {

    private val bangumiUserFileUfd = bangumiRootFileUfd.child("user")
    private val collectionRepositoryMap = hashMapOf<String, BangumiCollectionRepository>()


    val collectBusiness by lazy {
        detailSourceCase.getBangumiCollectBusiness()
    }


    fun getCollectRepository(cartoonIndex: CartoonIndex): BangumiCollectionRepository {
        return getCollectRepository(info, cartoonIndex)
    }

    private fun getCollectRepository(accountInfo: BangumiAccountController.BangumiAccountInfo, cartoonIndex: CartoonIndex): BangumiCollectionRepository {
        val temp = collectionRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            return temp
        }
        lock.withLock {
            return collectionRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiCollectionRepository(
                    folder = bangumiUserFileUfd ?: throw IllegalStateException("bangumi getUserFile null"),
                    accountInfo = accountInfo,
                    bangumiCollectBusiness = collectBusiness,
                    cartoonIndex = cartoonIndex,
                    scope = scope,
                )
            }
        }
    }
}