import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.heyanle.easy_bangumi_cm.business.media.entity.MediaInfo
import com.heyanle.easy_bangumi_cm.business.media.repository.dao.MediaInfoDao
import com.heyanle.easy_bangumi_cm.component.provider.path.PathProvider
import org.koin.core.component.KoinComponent

@Database(entities = [MediaInfo::class], version = 1)
@ConstructedBy(MediaDatabaseConstructor::class)
abstract class MediaDatabase : RoomDatabase() {
    companion object {
        const val DB_FILE_NAME = "media.db"
    }

    abstract fun mediaInfoDao(): MediaInfoDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MediaDatabaseConstructor : RoomDatabaseConstructor<MediaDatabase> {
    override fun initialize(): MediaDatabase
}
