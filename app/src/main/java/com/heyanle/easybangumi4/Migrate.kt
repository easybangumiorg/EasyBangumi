package com.heyanle.easybangumi4

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.base.theme.EasyThemeMode
import com.heyanle.easybangumi4.preferences.SettingMMKVPreferences
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.core.okkv

/**
 * preferences 更新
 * Created by HeYanLe on 2023/7/29 17:38.
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



    fun tryUpdate(
        context: Context
    ) {
        preferenceUpdate(
            context,
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
            Injekt.get(),
        )
    }

    private fun preferenceUpdate(
        context: Context,
        androidPreferenceStore: AndroidPreferenceStore,
        mmkvPreferenceStore: MMKVPreferenceStore,
        settingPreferences: SettingPreferences,
        sourcePreferences: SourcePreferences,
        settingMMKVPreferences: SettingMMKVPreferences,
    ) {

        val lastVersionCode = androidPreferenceStore.getInt("last_version_code", 0).get()
        val curVersionCode = BuildConfig.VERSION_CODE

        if (lastVersionCode < curVersionCode) {
            // 后续版本在这里加数据迁移


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
                settingPreferences.padMode.set(SettingPreferences.PadMode.values()[padModeOkkv])

                settingMMKVPreferences.webViewCompatible.set(webViewCompatibleOkkv)

                // 源配置变更
                var configOkkv by okkv("source_config", "[]")
                val list: List<SourcePreferences.SourceConfig> = configOkkv.jsonTo()
                val map = hashMapOf<String, SourcePreferences.SourceConfig>()
                list.forEach {
                    map[it.key] = it
                }
                sourcePreferences.configs.set(map)
            }
        }

        androidPreferenceStore.getInt("last_version_code", 0).set(curVersionCode)

    }


}