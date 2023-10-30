package com.heyanle.easybangumi4.cartoon.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/8/13 16:46.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonInfoDao {

    @Insert
    suspend fun insert(cartoonInfo: CartoonInfo)

    @Update
    suspend fun update(cartoonInfo: CartoonInfo)

    @Delete
    suspend fun delete(cartoonInfo: CartoonInfo)

    @Query("SELECT * FROM CartoonInfo ORDER BY createTime DESC")
    suspend fun getAll(): List<CartoonInfo>

    @Query("SELECT * FROM CartoonInfo ORDER BY createTime DESC")
    fun flowAll(): Flow<List<CartoonInfo>>

    @Query("SELECT * FROM CartoonInfo WHERE source=(:source)")
    suspend fun getAllBySource(source: String): List<CartoonInfo>
    @Query("SELECT * FROM CartoonInfo WHERE id=(:id) AND source=(:source) AND url=(:url)")
    suspend fun getByCartoonSummary(id: String, source: String, url: String): CartoonInfo?

    @Query("DELETE FROM CartoonInfo WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    suspend fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    suspend fun delete(cartoonInfo: List<CartoonInfo>){
        cartoonInfo.forEach {
            delete(it)
        }
    }

    @Transaction
    suspend fun modify(cartoonInfo: CartoonInfo) {
        val old = getByCartoonSummary(cartoonInfo.id, cartoonInfo.source, cartoonInfo.url)
        if (old == null) {
            insert(cartoonInfo.copy(createTime = System.currentTimeMillis(), lastUpdateTime = System.currentTimeMillis()))
        } else {
            update(cartoonInfo.copy(createTime = old.createTime, lastUpdateTime = System.currentTimeMillis()))
        }
    }

    @Transaction
    suspend fun modify(starList: List<CartoonInfo>, lastUpdateTime: Long? = null) {
        starList.forEach { cartoonInfo ->
            val old = getByCartoonSummary(cartoonInfo.id, cartoonInfo.source, cartoonInfo.url)
            if (old == null) {
                insert(cartoonInfo.copy(createTime = System.currentTimeMillis(), lastUpdateTime = lastUpdateTime?:cartoonInfo.lastUpdateTime))
            } else {
                update(cartoonInfo.copy(createTime = old.createTime, lastUpdateTime = lastUpdateTime?:cartoonInfo.lastUpdateTime))
            }
        }
    }

    @Transaction
    suspend fun migration(old: List<CartoonInfo>, new: List<CartoonInfo>){
        old.forEach {
            deleteByCartoonSummary(it.id, it.source, it.url)
        }
        new.forEach {
            modify(it)
        }
    }
    
}