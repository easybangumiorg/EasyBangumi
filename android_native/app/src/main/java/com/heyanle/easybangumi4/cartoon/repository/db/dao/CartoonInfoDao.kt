package com.heyanle.easybangumi4.cartoon.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonInfoDao {

    // info
    @Insert
    suspend fun insert(cartoonInfo: CartoonInfo)

    @Update
    suspend fun update(cartoonInfo: CartoonInfo)

    @Delete
    suspend fun delete(cartoonInfo: CartoonInfo)

    @Query("SELECT * FROM CartoonInfoV2")
    fun flowAll(): Flow<List<CartoonInfo>>

    @Query("SELECT * FROM CartoonInfoV2")
    fun getAll(): List<CartoonInfo>

    @Query("SELECT * FROM CartoonInfoV2 WHERE id=(:id) AND source=(:source)")
    suspend fun getByCartoonSummary(id: String, source: String): CartoonInfo?

    @Query("SELECT * FROM CartoonInfoV2 WHERE source=(:source)")
    suspend fun getAllBySource(source: String): List<CartoonInfo>

    @Query("DELETE FROM CartoonInfoV2 WHERE id=(:id) AND source=(:source)")
    suspend fun deleteByCartoonSummary(id: String, source: String)

    @Query("DELETE FROM CartoonInfoV2 WHERE 1=1")
    suspend fun clearAll()

    @Transaction
    suspend fun modify(cartoonInfo: CartoonInfo) {
        val old = getByCartoonSummary(cartoonInfo.id, cartoonInfo.source)
        if (old == null) {
            insert(cartoonInfo.copy(createTime = System.currentTimeMillis()))
        } else {
            update(cartoonInfo.copy(createTime = old.createTime))
        }
    }

    @Transaction
    suspend fun modify(cartoonInfo: List<CartoonInfo>) {
        cartoonInfo.forEach {
            modify(it)
        }
    }


    // history
    @Query("SELECT * FROM CartoonInfoV2 WHERE lastHistoryTime>0 ORDER BY lastHistoryTime DESC")
    fun flowAllHistory(): Flow<List<CartoonInfo>>

    @Query("SELECT * FROM CartoonInfoV2 WHERE lastHistoryTime>0 ORDER BY lastHistoryTime DESC")
    fun getAllHistory(): List<CartoonInfo>


    @Transaction
    suspend fun deleteHistory(cartoonInfo: CartoonInfo){
        modify(cartoonInfo.copy(lastHistoryTime = 0,))
    }

    @Transaction
    suspend fun deleteHistory(cartoon: List<CartoonInfo>){
        cartoon.forEach {
            modify(it.copy(lastHistoryTime = 0,))
        }

    }

    @Transaction
    suspend fun clearHistory() {
        getAllHistory().forEach {
            modify(it.copy(
                lastHistoryTime = 0,
            ))
        }

    }

    // star
    @Query("SELECT * FROM CartoonInfoV2 WHERE starTime>0")
    fun flowAllStar(): Flow<List<CartoonInfo>>

    suspend fun renameTag(oldTag: String, newTag: String) {
        modify(flowAllStar().first().map {
            it.renameTag(oldTag, newTag)
        })
    }

    @Transaction
    suspend fun deleteStar(cartoonInfo: CartoonInfo){
        modify(cartoonInfo.copy(starTime = 0, tags = "", upTime = 0))
    }

    @Transaction
    suspend fun deleteStar(cartoonInfoList: List<CartoonInfo>){
        cartoonInfoList.forEach {
            modify(it.copy(starTime = 0, tags = "", upTime = 0))
        }
    }




}