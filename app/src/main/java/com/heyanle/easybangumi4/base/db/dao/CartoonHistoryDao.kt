package com.heyanle.easybangumi4.base.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.base.entity.CartoonHistory
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonHistoryDao {

    @Insert
    suspend fun insert(cartoonHistory: CartoonHistory)

    @Update
    suspend fun update(cartoonHistory: CartoonHistory)

    @Delete
    suspend fun delete(cartoonHistory: CartoonHistory)

    @Query("DELETE FROM CartoonHistory WHERE 1=1")
    suspend fun clear()

    @Query("SELECT * FROM cartoonHistory ORDER BY createTime DESC")
    fun flowAllOrderByTime(): Flow<List<CartoonHistory>>

    @Query("SELECT * FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    suspend fun getFromCartoonSummary(id: String, source: String, detailUrl: String): CartoonHistory?

    @Query("DELETE FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    suspend fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    suspend fun delete(cartoonHistory: List<CartoonHistory>){
        cartoonHistory.forEach {
            delete(it)
        }
    }

    @Transaction
    suspend fun modify(cartoonHistory: CartoonHistory) {
        val old = getFromCartoonSummary(cartoonHistory.id, cartoonHistory.source, cartoonHistory.url)
        if (old == null) {
            insert(cartoonHistory.copy(createTime = System.currentTimeMillis()))
        } else {
            update(cartoonHistory.copy(createTime = old.createTime))
        }
    }

}