package com.heyanle.easybangumi4

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by HeYanLe on 2023/10/29 15:08.
 * https://github.com/heyanLE
 */
object Migrate {

    object AppDB {
        fun getDBMigration() = listOf(
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
        )

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN reversal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN watchProcess TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS CartoonTag (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, label TEXT NOT NULL DEFAULT '', 'order' INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN sourceName TEXT NOT NULL DEFAULT ''")
            }
        }
    }

    object CacheDB {
        fun getDBMigration() = emptyList<Migration>()
    }

}