package com.heyanle.easybangumi4.cartoon.old.repository

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoOld
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/10/29 15:04.
 * https://github.com/heyanLE
 */
class CartoonRepository(
    private val settingPreferences: SettingPreferences,
    private val cartoonInfoDao: CartoonInfoDao,
    private val cartoonNetworkDataSource: CartoonNetworkDataSource,
    private val sourceStateCase: SourceStateCase
) {


    suspend fun awaitCartoonInfoWithPlayLines(
        id: String,
        source: String,
        url: String,
        time: Long = System.currentTimeMillis(),
    ): DataResult<Pair<CartoonInfoOld, List<PlayLine>>> {
        return withContext(Dispatchers.IO) {
            val local = cartoonInfoDao.getByCartoonSummary(id, source, url)
            val oldUpdateTime = local?.lastUpdateTime ?: 0L
            if (local == null || local.getPlayLine()
                    .isEmpty() || time > oldUpdateTime
            ) {
                // 过期或者不存在 走网络
                val netResult = cartoonNetworkDataSource.awaitCartoonWithPlayLines(id, source, url)
                // 异步更新
                launch(Dispatchers.IO) {
                    if (netResult is DataResult.Ok) {
                        val sourceName =
                            sourceStateCase.awaitBundle().source(source)?.label
                                ?: ""
                        val info = CartoonInfoOld.fromCartoon(
                            netResult.data.first,
                            sourceName,
                            netResult.data.second
                        )
                        cartoonInfoDao.modify(info)
                    }
                }


                val res = netResult.map {
                    val sourceName =
                        sourceStateCase.awaitBundle().source(source)?.label ?: ""
                    CartoonInfoOld.fromCartoon(
                        it.first,
                        sourceName,
                        it.second
                    ) to it.second
                }
                // 如果网络错误，但是缓存还在，先返回缓存先用着
                if (res !is DataResult.Error || local == null) {
                    return@withContext res
                }
            }
            return@withContext DataResult.ok(local to local.getPlayLine())
        }
    }


}