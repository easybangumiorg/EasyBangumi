package com.heyanle.easybangumi4.base.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.base.entity.CartoonDownload

/**
 * 本地数据库，后续迁移时不需要跟着迁移的表
 * Created by HeYanLe on 2023/8/13 22:37.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonDownload::class
    ],
    autoMigrations = [],
    version = 1,
    exportSchema = true
)
abstract class AppLocalDatabase: RoomDatabase() {

    abstract fun cartoonDownloadDao(): CartoonDownloadDao
    val cartoonDownload: CartoonDownloadDao by lazy {cartoonDownloadDao() }

    companion object {
        // 还未使用，先不新建了
        fun build(context: Context): AppLocalDatabase {
            return Room.databaseBuilder(
                context,
                AppLocalDatabase::class.java, "easy_cartoon_local"
            ).apply {
                Migrate.AppLocalDB.getDBMigration().forEach {
                    addMigrations(it)
                }
            }.build()
        }
    }

}