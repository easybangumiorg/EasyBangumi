package com.heyanle.easybangumi4

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.SearchHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoOld
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoV1
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.old.repository.db.AppDatabase
import com.heyanle.easybangumi4.cartoon.old.repository.db.CacheDatabase
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.extension.store.OfficialExtensionItem
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Math.*
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/10/29 15:08.
 * https://github.com/heyanLE
 */
object Migrate {

    private val _isMigrating = MutableStateFlow<Boolean>(true)
    val isMigrating = _isMigrating.asStateFlow()

    private val scope = MainScope()

    object CartoonDB {
        fun getDBMigration() = listOf<Migration>()
    }

    // 弃用数据库
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
                db.execSQL("CREATE TABLE CartoonTag (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, label TEXT NOT NULL DEFAULT '', 'order' INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN sourceName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonHistory ADD COLUMN lastLineId TEXT NOT NULL DEFAULT ''")

                db.execSQL("ALTER TABLE CartoonHistory ADD COLUMN lastEpisodeId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE CartoonHistory ADD COLUMN lastEpisodeOrder INTEGER NOT NULL DEFAULT 0")

            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
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

    fun update(context: Context) {
        preferenceUpdate(
            context,
            Injekt.get(),
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
        cartoonDatabase: CartoonDatabase,
    ) {

        val lastVersionCode = androidPreferenceStore.getInt("last_version_code", 0).get()
        val curVersionCode = BuildConfig.VERSION_CODE



        if (lastVersionCode < curVersionCode) {

            scope.launch(Dispatchers.IO) {
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
                    val list: List<SourceConfig> = configOkkv.jsonTo() ?: emptyList()
                    val map = hashMapOf<String, SourceConfig>()
                    list.forEach {
                        map[it.key] = it
                    }
                    sourcePreferences.configs.set(map)
                }

                // 73
                if (lastVersionCode < 73) {

                    // 数据库变更
                    migrateCartoonDatabase73(
                        AppDatabase.build(context),
                        CacheDatabase.build(context),
                        cartoonDatabase
                    )
                }


                // 78
                if (lastVersionCode < 78) {
                    migrateCartoonDatabase78(cartoonDatabase)
                }

                // 82
                if (lastVersionCode < 82) {
                    // 下载的 apk 之前后缀多打了一个 . ，简单修正一下
                    File(context.getFilePath("extension")).listFiles()?.forEach {
                        if (it != null && it.name.endsWith("..easybangumi.apk")) {
                            it.renameTo(
                                File(
                                    context.getFilePath("extension"),
                                    it.name.replace("..easybangumi.apk", ".easybangumi.apk")
                                )
                            )
                        }
                    }
                    val extensionItemJson =
                        File(context.getFilePath("extension-store"), "official.json")
                    if(extensionItemJson.exists()){
                        val map = hashMapOf<String, OfficialExtensionItem>()
                        extensionItemJson.readText().jsonTo<Map<String, OfficialExtensionItem>>()
                            ?.asIterable()?.forEach {
                                if (it.value.realFilePath.endsWith("..easybangumi.apk")) {
                                    map[it.key] = it.value.copy(
                                        realFilePath = it.value.realFilePath.replace(
                                            "..easybangumi.apk",
                                            ".easybangumi.apk"
                                        )
                                    )
                                } else {
                                    map[it.key] = it.value
                                }
                            }
                        extensionItemJson.delete()
                        extensionItemJson.createNewFile()
                        extensionItemJson.writeText(map.toJson<Map<String, OfficialExtensionItem>>())
                    }

                }

                if (lastVersionCode < 85) {
                    val dbFileO = context.getDatabasePath("easy_bangumi_cartoon")
                    val dbFileShmO = context.getDatabasePath("easy_bangumi_cartoon-shm")
                    val dbFileWalO = context.getDatabasePath("easy_bangumi_cartoon-wal")
                    val dbFile = context.getDatabasePath("easy_bangumi_cartoon.db")
                    val dbFileShm = context.getDatabasePath("easy_bangumi_cartoon.db-shm")
                    val dbFileWal = context.getDatabasePath("easy_bangumi_cartoon.db-wal")

                    dbFile.delete()
                    dbFileShm.delete()
                    dbFileWal.delete()
                    dbFileO.renameTo(dbFile)
                    dbFileShmO.renameTo(dbFileShm)
                    dbFileWalO.renameTo(dbFileWal)
                }


                if (lastVersionCode < 87) {
                    val dbFileO = context.getDatabasePath("easy_bangumi_cartoon.db")
                    val dbFileShmO = context.getDatabasePath("easy_bangumi_cartoon.db-shm")
                    val dbFileWalO = context.getDatabasePath("easy_bangumi_cartoon.db-wal")
                    val dbFile = context.getDatabasePath("easy_bangumi_cartoon")
                    val dbFileShm = context.getDatabasePath("easy_bangumi_cartoon-shm")
                    val dbFileWal = context.getDatabasePath("easy_bangumi_cartoon-wal")

                    dbFile.delete()
                    dbFileShm.delete()
                    dbFileWal.delete()
                    dbFileO.renameTo(dbFile)
                    dbFileShmO.renameTo(dbFileShm)
                    dbFileWalO.renameTo(dbFileWal)
                }

                // 在这里添加新的迁移代码

                androidPreferenceStore.getInt("last_version_code", 0).set(curVersionCode)
                _isMigrating.update {
                    false
                }
            }

        } else {
            _isMigrating.update {
                false
            }
        }

    }


    // 数据库变更 78 ==================================================

    private suspend fun migrateCartoonDatabase78(
        cartoonDatabase: CartoonDatabase,
    ) {
        val newCartoonInfoDao = cartoonDatabase.cartoonInfo
        val otherDao = cartoonDatabase.other
        val cartoonInfoV1 = otherDao.getAllCartoonInfoV1()
        val map = HashMap<String, CartoonInfoV1>()
        cartoonInfoV1.forEach {
            val key = "${it.id}-${URLEncoder.encode(it.source, "utf-8")}}"
            val old = map[key]
            if (old == null) {
                map[key] = it
            } else {
                if (!old.isDetailed && it.isDetailed) {
                    map[key] = it
                } else if (it.isDetailed) {
                    if (it.lastUpdateTime >= old.lastUpdateTime) {
                        map[key] = it
                    }
                }
            }
        }
        map.asIterable().forEach {
            val no = newCartoonInfoDao.getByCartoonSummary(it.value.id, it.value.source)
            if (no == null) {
                newCartoonInfoDao.insert(it.value.toNewCartoon())
            }
        }
    }

    private fun CartoonInfoV1.toNewCartoon() = CartoonInfo(
        id,
        source,
        name,
        coverUrl,
        intro,
        url,
        isDetailed,
        genre,
        description,
        updateStrategy,
        isUpdate,
        status,
        lastUpdateTime,
        isShowLine,
        sourceName,
        reversal,
        sortByKey,
        isPlayLineLoad,
        playLineString,
        tags,
        starTime,
        upTime,
        lastHistoryTime,
        lastPlayLineEpisodeString,
        lastLineId,
        lastLinesIndex,
        lastLineLabel,
        lastEpisodeId,
        lastEpisodeOrder,
        lastEpisodeIndex,
        lastEpisodeLabel,
        lastTotalTile,
        lastProcessTime,
        createTime
    )

    // ↑ 数据库变更 78 ===================================================


    // 数据库变更 73 ===================================================

    data class OldSummary73(
        val id: String,              // 标识，由源自己支持，用于区分番剧
        val source: String,
        val url: String,
    ) {
        fun toIdentify() =
            "${id}-${URLEncoder.encode(source, "utf-8")}-${URLEncoder.encode(url, "utf-8")}"
    }

    private suspend fun migrateCartoonDatabase73(
        appDatabase: AppDatabase,
        cacheDatabase: CacheDatabase,
        cartoonDatabase: CartoonDatabase,
    ) {
        val needMigrateSummary = hashSetOf<OldSummary73>()

        val starMap = hashMapOf<String, CartoonStar>()
        val historyMap = hashMapOf<String, CartoonHistory>()
        val infoMap = hashMapOf<String, CartoonInfoOld>()


        appDatabase.cartoonStar.getAll().forEach {
            val sum = OldSummary73(it.id, it.source, it.url)
            needMigrateSummary.add(sum)
            starMap[sum.toIdentify()] = it
        }
        appDatabase.cartoonHistory.getAll().forEach {
            val sum = OldSummary73(it.id, it.source, it.url)
            needMigrateSummary.add(sum)
            historyMap[sum.toIdentify()] = it
        }
        cacheDatabase.cartoonInfo.getAll().forEach {
            val sum = OldSummary73(it.id, it.source, it.url)
            needMigrateSummary.add(sum)
            infoMap[sum.toIdentify()] = it
        }
        cartoonDatabase.cartoonInfo.clearAll()
        needMigrateSummary.flatMap {
            val star = starMap[it.toIdentify()]
            val history = historyMap[it.toIdentify()]
            val infoOld = infoMap[it.toIdentify()]
            val info = toCartoonInfo(it, star, history, infoOld)
            if (info == null) {
                emptyList()
            } else {
                listOf(info)
            }
        }.forEach {
            cartoonDatabase.cartoonInfo.modify(it)
        }

        cartoonDatabase.searchHistory.clear()
        appDatabase.searchHistory.getAll().forEach {
            cartoonDatabase.searchHistory.modify(SearchHistory(it.id, it.timestamp, it.content))
        }
        appDatabase.searchHistory.clear()


        cartoonDatabase.cartoonTag.clear()
        appDatabase.cartoonTag.getAll().forEach {
            cartoonDatabase.cartoonTag.insert(CartoonTag(it.id, it.label, it.order))
        }
        appDatabase.cartoonTag.clear()
    }

    private fun toCartoonInfo(
        cartoonSummary: OldSummary73,
        cartoonStar: CartoonStar?,
        cartoonHistory: CartoonHistory?,
        cartoonInfo: CartoonInfoOld?,
    ): CartoonInfo? {
        return CartoonInfo(
            id = cartoonSummary.id,
            source = cartoonSummary.source,
            url = cartoonSummary.url,

            name = cartoonStar?.title ?: cartoonHistory?.name ?: cartoonInfo?.title ?: return null,
            coverUrl = cartoonStar?.coverUrl ?: cartoonHistory?.cover ?: cartoonInfo?.coverUrl
            ?: return null,
            intro = cartoonStar?.intro ?: cartoonHistory?.intro ?: cartoonInfo?.intro
            ?: return null,

            isDetailed = false, // 迁移就让他刷新一下数据吧

            isShowLine = cartoonInfo?.isShowLine ?: true,
            sourceName = cartoonStar?.sourceName ?: cartoonInfo?.sourceName ?: return null,
            reversal = cartoonStar?.reversal ?: false,
            sortByKey = cartoonStar?.sortByKey ?: "",

            tags = cartoonStar?.tags ?: cartoonInfo?.tags ?: "",
            starTime = cartoonStar?.createTime ?: 0L,
            upTime = cartoonStar?.upTime ?: 0L,

            isPlayLineLoad = false,

            lastHistoryTime = cartoonHistory?.createTime ?: 0L,

            lastLineId = cartoonHistory?.lastLineId ?: "",
            lastLinesIndex = cartoonHistory?.lastLinesIndex ?: 0,
            lastLineLabel = cartoonHistory?.lastLineTitle ?: "",

            lastEpisodeId = cartoonHistory?.lastEpisodeId ?: "",
            lastEpisodeIndex = cartoonHistory?.lastEpisodeIndex ?: 0,
            lastEpisodeLabel = cartoonHistory?.lastEpisodeTitle ?: "",
            lastEpisodeOrder = cartoonHistory?.lastEpisodeOrder ?: 0,

            lastProcessTime = cartoonHistory?.lastProcessTime ?: 0,

            createTime = (cartoonHistory?.createTime ?: Long.MAX_VALUE).coerceAtMost(
                (cartoonInfo?.createTime ?: Long.MAX_VALUE).coerceAtMost(
                    cartoonStar?.createTime ?: Long.MAX_VALUE
                )
            ).coerceAtMost(System.currentTimeMillis())
        )
    }

    // ↑ 数据库变更 73 ===================================================
    private fun controllerUpdate(
        context: Context,
    ) {

        val rootFolder = File(context.getFilePath("download"))

        // 本地番剧 json 文件更新
        val localCartoonJson = File(rootFolder, "local.json")
        val localCartoonJsonTem = File(rootFolder, "local.json.bk")

        // 下载记录 json 文件更新
        val downloadItemJson = File(rootFolder, "item.json")
        val downloadItemJsonTemp = File(rootFolder, "item.json.bk")
    }

}


