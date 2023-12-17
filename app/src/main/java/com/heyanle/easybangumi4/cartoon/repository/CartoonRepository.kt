package com.heyanle.easybangumi4.cartoon.repository

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoOld
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao

import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
class CartoonRepository(
    private val settingPreferences: SettingPreferences,
    private val cartoonInfoDao: CartoonInfoDao,
    private val cartoonNetworkDataSource: CartoonNetworkDataSource,
    private val sourceStateCase: SourceStateCase
) {

    suspend fun awaitCartoonInfoWIthPlayLines(
        id: String,
        source: String,
        url: String,
        time: Long = System.currentTimeMillis(),
    ): DataResult<CartoonInfo> {
        return withContext(Dispatchers.IO) {
            val local = cartoonInfoDao.getByCartoonSummary(id, source, url)
            val oldUpdateTime = local?.lastUpdateTime ?: 0L
            if (local != null && local.isDetailed && local.isPlayLineLoad && time <= oldUpdateTime) {
                return@withContext DataResult.ok(local)
            }
            val netResult = cartoonNetworkDataSource.awaitCartoonWithPlayLines(id, source, url)

            // 异步缓存
            launch(Dispatchers.IO) {
                if (netResult is DataResult.Ok) {
                    val sourceName =
                        sourceStateCase.awaitBundle().source(source)?.label
                            ?: ""
                    if (local != null) {
                        cartoonInfoDao.modify(
                            local.copyFromCartoon(
                                netResult.data.first,
                                sourceName,
                                netResult.data.second
                            )
                        )
                    } else {
                        cartoonInfoDao.modify(
                            CartoonInfo.fromCartoon(
                                netResult.data.first,
                                sourceName,
                                netResult.data.second
                            )
                        )
                    }
                }
            }

            val res = netResult.map {
                val sourceName =
                    sourceStateCase.awaitBundle().source(source)?.label ?: ""
                local?.copyFromCartoon(
                    it.first,
                    sourceName,
                    it.second
                )?:CartoonInfo.fromCartoon(
                    it.first,
                    sourceName,
                    it.second
                )
            }
            // 如果网络错误，但是缓存还在，先返回缓存先用着
            if (res !is DataResult.Error<CartoonInfo> || local == null) {
                return@withContext res
            }

            return@withContext DataResult.ok(local)

        }
    }

}