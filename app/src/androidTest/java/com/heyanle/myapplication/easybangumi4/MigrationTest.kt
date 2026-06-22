package com.heyanle.myapplication.easybangumi4

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.cartoon.old.repository.db.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by HeYanLe on 2023/6/14 15:34.
 * https://github.com/heyanLE
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        helper.createDatabase(TEST_DB, 9).apply {
            close()
        }
    }
}
