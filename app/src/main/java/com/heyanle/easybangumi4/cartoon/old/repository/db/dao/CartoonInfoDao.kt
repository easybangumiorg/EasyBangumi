package com.heyanle.easybangumi4.cartoon.old.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoOld
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/8/13 16:46.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonInfoDao {

    @Insert
    suspend fun insert(cartoonInfoOld: CartoonInfoOld)

    @Update
    suspend fun update(cartoonInfoOld: CartoonInfoOld)

    @Delete
    suspend fun delete(cartoonInfoOld: CartoonInfoOld)

    @Query("SELECT * FROM CartoonInfo ORDER BY createTime DESC")
    suspend fun getAll(): List<CartoonInfoOld>

    @Query("SELECT * FROM CartoonInfo ORDER BY createTime DESC")
    fun flowAll(): Flow<List<CartoonInfoOld>>

    @Query("SELECT * FROM CartoonInfo WHERE source=(:source)")
    suspend fun getAllBySource(source: String): List<CartoonInfoOld>
    @Query("SELECT * FROM CartoonInfo WHERE id=(:id) AND source=(:source) AND url=(:url)")
    suspend fun getByCartoonSummary(id: String, source: String, url: String): CartoonInfoOld?

    @Query("DELETE FROM CartoonInfo WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    suspend fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    suspend fun delete(cartoonInfoOld: List<CartoonInfoOld>){
        cartoonInfoOld.forEach {
            delete(it)
        }
    }

    @Transaction
    suspend fun modify(cartoonInfoOld: CartoonInfoOld) {
        val old = getByCartoonSummary(cartoonInfoOld.id, cartoonInfoOld.source, cartoonInfoOld.url)
        if (old == null) {
            insert(cartoonInfoOld.copy(createTime = System.currentTimeMillis(), lastUpdateTime = System.currentTimeMillis()))
        } else {
            update(cartoonInfoOld.copy(createTime = old.createTime, lastUpdateTime = System.currentTimeMillis()))
        }
    }

    @Transaction
    suspend fun modify(starList: List<CartoonInfoOld>, lastUpdateTime: Long? = null) {
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
    suspend fun migration(old: List<CartoonInfoOld>, new: List<CartoonInfoOld>){
        old.forEach {
            deleteByCartoonSummary(it.id, it.source, it.url)
        }
        new.forEach {
            modify(it)
        }
    }
    
}