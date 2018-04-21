package biz.ajoshi.kolchat

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import biz.ajoshi.kolchat.persistence.ChannelDao
import biz.ajoshi.kolchat.persistence.KolDB
import com.facebook.drawee.backends.pipeline.Fresco
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by ajoshi on 7/14/17.
 */
class KolChatApp : Application() {
    companion object {
        var database: KolDB? = null
    }

    override fun onCreate() {
        super.onCreate()
        // migrations are probably dags to allow db upgrades from one version to another?
        Fabric.with(this, Crashlytics())
        val migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // if I have to write sql (and can't even reuse the db name const) what's the point of room?
                database.execSQL("DROP TABLE `" + ChannelDao.CHANNEL_DB_NAME + "`")
                database.execSQL("CREATE TABLE IF NOT EXISTS `ChatChannel` (`id` TEXT NOT NULL, `name` TEXT," +
                        " `isPrivate` INTEGER NOT NULL, `lastMessage` TEXT, `last_message_time` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                // could I have just done "ALTER TABLE `ChatChannel` ALTER COLUMN `id` TEXT NOT NULL"? we'll never know
            }
        }
        database = Room.databaseBuilder(this, KolDB::class.java, "KolDB").addMigrations(migration).build()
        Fresco.initialize(this)
    }
}
