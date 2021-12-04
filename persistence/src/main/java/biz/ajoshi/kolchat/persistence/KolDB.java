package biz.ajoshi.kolchat.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import biz.ajoshi.kolchat.persistence.chat.ChannelDao;
import biz.ajoshi.kolchat.persistence.chat.ChatChannel;
import biz.ajoshi.kolchat.persistence.chat.ChatMessage;
import biz.ajoshi.kolchat.persistence.chat.MessageDao;

/**
 * Created by ajoshi on 7/14/17.
 */

@Database(entities = {ChatMessage.class, ChatChannel.class}, version = 7)
public abstract class KolDB extends RoomDatabase {
    private static KolDB DATABASE;

    public static KolDB getDb() {
        return DATABASE;
    }

    // anon inner class- is this a problem? I doubt it since the db is already a singleton
    /* package */ static Migration MIGRATION_4_5 = new Migration(4, 5) {
        public void migrate(SupportSQLiteDatabase database) {
            // added new column for last viewed timestamp (for "you have read up to here" bar)
            database.execSQL("ALTER TABLE chatchannel "
                    + " ADD COLUMN lastTimeUserViewedChannel INTEGER NOT NULL DEFAULT 0");
        }
    };
    // no migration from v5 to 6 since I have no clue how to alter a table to alter the primary key

    public static void createDb(Context ctx) {
        if (DATABASE == null) {
            // migrations are probably dags to allow db upgrades from one version to another?
            Migration migration3_4 = new Migration(3, 4) {
                public void migrate(SupportSQLiteDatabase database) {
                    // if I have to write sql (and can't even reuse the db name const) what's the point of room?
                    database.execSQL("DROP TABLE `" + ChannelDao.CHANNEL_DB_NAME + "`");
                    database.execSQL("CREATE TABLE IF NOT EXISTS `ChatChannel` (`id` TEXT NOT NULL, `name` TEXT," +
                            " `isPrivate` INTEGER NOT NULL, `lastMessage` TEXT, `last_message_time` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                    // could I have just done "ALTER TABLE `ChatChannel` ALTER COLUMN `id` TEXT NOT NULL"? we'll never know
                }
            };

            DATABASE = Room.databaseBuilder(ctx, KolDB.class, "KolDB")
                    .addMigrations(migration3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // delete db instead of crashing if we had no upgrade path
                    .build();
        }
    }

    public abstract ChannelDao ChannelDao();

    public abstract MessageDao MessageDao();
}
