package org.easybangumi.next.shared.bangumi.data

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCollectionRepository
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.bangumi.source.BangumiCollectComponent.CollectionsPagingSource
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import kotlin.concurrent.Volatile

/**
 * bangumi 登录态有关数据
 * Created by heyanlin on 2025/11/5.
 */
class BangumiUserDataProvider(
    private val info: BangumiAccountController.BangumiAccountInfo,
    private val bangumiRootFileUfd: UFD,
    private val sourceCase: SourceCase,
    private val lock: ReentrantLock,
    private val scope: CoroutineScope,
) {

    private val bangumiUserFileUfd = bangumiRootFileUfd.child("user")
    private val collectionRepositoryMap = hashMapOf<String, BangumiCollectionRepository>()

    @Volatile
    var lastCollectChangeTime = 0L
        private set

    val collectBusiness by lazy {
        sourceCase.getBangumiCollectBusiness()
    }

    suspend fun changeBangumiCollectType(
        bangumiCollectType: BangumiConst.BangumiCollectType,
        cartoonIndex: CartoonIndex,
        updateRepository: Boolean = true,
    ): DataState<BgmRsp<String?>> {
        val resp = collectBusiness.run {
            changeCollectType(
                info.username,
                info.token,
                bangumiCollectType.type.toString(),
                cartoonIndex.id
            )
        }
        lastCollectChangeTime = Clock.System.now().toEpochMilliseconds()
        if (updateRepository && resp.isOk()) {
            val repository = getCollectRepository(cartoonIndex)
            repository.refresh()
        }
        return resp
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

    fun getCollectPagingSource(
        type: BangumiConst.BangumiCollectType,
    ): CollectionsPagingSource {
        return collectBusiness.runDirect { createEpisodePagingSource(
            type = type.type,
            username = info.username,
            token = info.token)
        }
    }
}