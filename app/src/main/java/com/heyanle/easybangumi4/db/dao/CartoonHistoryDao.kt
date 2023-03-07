package com.heyanle.easybangumi4.db.dao

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.db.entity.CartoonHistory

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonHistoryDao {

    @Insert
    fun insert(cartoonHistory: CartoonHistory)

    @Update
    fun modify(cartoonHistory: CartoonHistory)

    @Delete
    fun delete(cartoonHistory: CartoonHistory)

    @Query("DELETE FROM CartoonHistory WHERE 1=1")
    fun clear()

    @Query("SELECT * FROM cartoonHistory ORDER BY createTime DESC")
    fun getAllOrderByTime(): PagingSource<Int, CartoonHistory>

    @Query("SELECT * FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    fun getFromCartoonSummary(id: String, source: String, detailUrl: String): CartoonHistory?

    @Query("DELETE FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    fun insertOrModify(history: CartoonHistory) {

        val query = getFromCartoonSummary(history.id, history.source, history.url)
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
            Log.d("CartoonHistoryDao", "insertOrModify $lastP")

            modify(
                history.copy(
                    historyId = query.historyId,
                    lastProcessTime = lastP,
                    createTime = System.currentTimeMillis()
                )
            )
        } else {
            insert(history.copy(createTime = System.currentTimeMillis()))
        }
    }
}