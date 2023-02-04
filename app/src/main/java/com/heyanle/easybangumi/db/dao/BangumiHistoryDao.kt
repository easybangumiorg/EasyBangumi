package com.heyanle.easybangumi.db.dao

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.*
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.db.entity.SearchHistory

/**
 * Created by HeYanLe on 2023/1/29 21:20.
 * https://github.com/heyanLE
 */
@Dao
interface BangumiHistoryDao {

    @Insert
    fun insert(bangumiHistory: BangumiHistory)

    @Update
    fun modify(bangumiHistory: BangumiHistory)

    @Delete
    fun delete(bangumiHistory: BangumiHistory)

    @Query("SELECT * FROM BangumiHistory ORDER BY createTime DESC")
    fun getAllOrderByTime(): PagingSource<Int, BangumiHistory>

    @Query("SELECT * FROM BangumiHistory WHERE source=(:source) AND detailUrl=(:detailUrl)")
    fun getFromBangumiSummary(source: String, detailUrl: String): BangumiHistory?

    @Transaction
    fun insertOrModify(history: BangumiHistory) {

        val query = getFromBangumiSummary(history.source, history.detailUrl)
        if (query != null) {
            val lastP = if (history.lastProcessTime == -1L) {
                if (history.lastLinesIndex == query.lastLinesIndex && history.lastEpisodeIndex == query.lastEpisodeIndex) {
                    query.lastProcessTime
                } else {
                    0
                }
            } else {
                history.lastProcessTime
            }
            Log.d("BangumiHistoryDao", "insertOrModify $lastP")

            modify(
                history.copy(
                    id = query.id,
                    lastProcessTime = lastP,
                    createTime = System.currentTimeMillis()
                )
            )
        } else {
            insert(history.copy(createTime = System.currentTimeMillis()))
        }
    }

}