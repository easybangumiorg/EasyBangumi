package com.heyanle.easybangumi4

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.core.okkv
import java.io.File

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
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonHistory ADD COLUMN lastLineId TEXT NOT NULL DEFAULT '', lastEpisodeId TEXT NOT NULL DEFAULT '', lastEpisodeOrder INTEGER NOT NULL DEFAULT -1")
            }
        }

        private val MIGRATION_7_8 = object: Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN upTime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN lastWatchTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN sortByKey TEXT NOT NULL DEFAULT ''")
            }
        }
    }

    object CacheDB {
        fun getDBMigration() = emptyList<Migration>()
    }

    fun update(context: Context){
        preferenceUpdate(
            context,
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
        )
        controllerUpdate(context)
    }



    private fun preferenceUpdate(
        context: Context,
        androidPreferenceStore: AndroidPreferenceStore,
        mmkvPreferenceStore: MMKVPreferenceStore,
        heKVPreferenceStore: HeKVPreferenceStore,
        settingPreferences: SettingPreferences,
        sourcePreferences: SourcePreferences,
        settingMMKVPreferences: SettingMMKVPreferences,
    ) {

        val lastVersionCode = androidPreferenceStore.getInt("last_version_code", 0).get()
        val curVersionCode = BuildConfig.VERSION_CODE

        if (lastVersionCode < curVersionCode) {

            // 65
            if (lastVersionCode < 65) {
                // preference 架构变更

                // 主题存储变更
                val themeModeOkkv by okkv("theme_mode", EasyThemeMode.Default.name)
                val darkModeOkkv by okkv("dark_mode", SettingPreferences.DarkMode.Auto.name)
                val isDynamicColorOkkv by okkv<Boolean>("is_dynamic_color", def = true)

                settingPreferences.themeMode.set(EasyThemeMode.valueOf(themeModeOkkv))
                settingPreferences.darkMode.set(SettingPreferences.DarkMode.valueOf(darkModeOkkv))
                settingPreferences.isThemeDynamic.set(isDynamicColorOkkv)

                // 其他配置变更
                val isPrivateOkkv by okkv("inPrivate", def = false)
                val padModeOkkv by okkv("padMode", def = 0)
                val webViewCompatibleOkkv by okkv("webViewCompatible", def = false)

                settingPreferences.isInPrivate.set(isPrivateOkkv)
                settingPreferences.padMode.set(SettingPreferences.PadMode.entries[padModeOkkv])

                settingMMKVPreferences.webViewCompatible.set(webViewCompatibleOkkv)

                // 源配置变更
                val configOkkv by okkv("source_config", "[]")
                val list: List<SourceConfig> = configOkkv.jsonTo()?: emptyList()
                val map = hashMapOf<String, SourceConfig>()
                list.forEach {
                    map[it.key] = it
                }
                sourcePreferences.configs.set(map)
            }
        }

        androidPreferenceStore.getInt("last_version_code", 0).set(curVersionCode)

    }

    private fun controllerUpdate(
        context: Context,
    ){

        val rootFolder = File(context.getFilePath("download"))

        // 本地番剧 json 文件更新
        val localCartoonJson = File(rootFolder, "local.json")
        val localCartoonJsonTem = File(rootFolder, "local.json.bk")

        // 下载记录 json 文件更新
        val downloadItemJson = File(rootFolder, "item.json")
        val downloadItemJsonTemp = File(rootFolder, "item.json.bk")
    }

}


