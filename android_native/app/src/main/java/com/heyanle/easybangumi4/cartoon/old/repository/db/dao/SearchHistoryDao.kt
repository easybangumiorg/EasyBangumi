package com.heyanle.easybangumi4.cartoon.old.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.cartoon.old.entity.SearchHistoryOld
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/3/29 20:49.
 * https://github.com/heyanLE
 */
@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory WHERE content=(:content)")
    suspend fun get(content: String): SearchHistoryOld?

    @Query("SELECT * FROM SearchHistory ORDER BY timestamp DESC")
    suspend fun getAll(): List<SearchHistoryOld>

    @Query("SELECT content FROM SearchHistory ORDER BY timestamp DESC LIMIT 10")
    fun flowTopContent(): Flow<List<String>>

    @Insert
    suspend fun insert(historyBean: SearchHistoryOld)

    @Update
    suspend fun modify(historyBean: SearchHistoryOld)

    @Delete
    suspend fun delete(historyBean: SearchHistoryOld)

    @Query("DELETE FROM SearchHistory WHERE 1=1")
    suspend fun clear()

    @Transaction
    suspend fun insertOrModify(content: String) {
        val query = get(content)
        if (query != null) {
            modify(query.copy(timestamp = System.currentTimeMillis()))
        } else {
            insert(SearchHistoryOld(timestamp = System.currentTimeMillis(), content = content))
        }
    }
}