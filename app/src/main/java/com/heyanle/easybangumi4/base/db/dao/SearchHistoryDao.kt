package com.heyanle.easybangumi4.base.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.base.entity.SearchHistory

/**
 * Created by HeYanLe on 2023/3/29 20:49.
 * https://github.com/heyanLE
 */
@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory WHERE content=(:content)")
    fun get(content: String): SearchHistory?

    @Query("SELECT * FROM SearchHistory ORDER BY timestamp DESC")
    fun getAll(): List<SearchHistory>

    @Query("SELECT content FROM SearchHistory ORDER BY timestamp DESC LIMIT 10")
    fun getAllContent(): List<String>

    @Insert
    fun insert(historyBean: SearchHistory)

    @Update
    fun modify(historyBean: SearchHistory)

    @Delete
    fun delete(historyBean: SearchHistory)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()

    @Transaction
    fun insertOrModify(content: String) {
        val query = get(content)
        if (query != null) {
            modify(query.copy(timestamp = System.currentTimeMillis()))
        } else {
            insert(SearchHistory(timestamp = System.currentTimeMillis(), content = content))
        }
    }
}