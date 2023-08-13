package com.heyanle.easybangumi4.exo

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider

/**
 * Created by HeYanLe on 2023/8/13 13:53.
 * https://github.com/heyanLE
 */
@UnstableApi
class MediaCacheDB(context: Context) :
    SQLiteOpenHelper(context, "exoplayer_music_cache.db", null, 1),
    DatabaseProvider {
    override fun onCreate(db: SQLiteDatabase) = Unit
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val columns = arrayOf("type", "name")
        db.query("sqlite_master", columns, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getString(0)
                val name = cursor.getString(1)
                if ("sqlite_sequence" != name) {
                    // If it's not an SQL-controlled entity, drop it
                    val sql = "DROP $type IF EXISTS $name"
                    try {
                        db.execSQL(sql)
                    } catch (e: SQLException) {
                        Log.e("MusicCacheDB", "Error executing $sql", e)
                    }
                }
            }
        }
    }
}