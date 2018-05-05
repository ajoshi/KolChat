package biz.ajoshi.kolchat.persistence;

import biz.ajoshi.kolchat.persistence.chat.ChannelDao;
import biz.ajoshi.kolchat.persistence.chat.ChatChannel;
import biz.ajoshi.kolchat.persistence.chat.ChatMessage;
import biz.ajoshi.kolchat.persistence.chat.MessageDao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

/**
 * Created by ajoshi on 7/14/17.
 */

@Database(entities = { ChatMessage.class, ChatChannel.class }, version = 4)
public abstract class KolDB extends RoomDatabase {
    private static KolDB DATABASE;

    public static KolDB getDb() {
        return DATABASE;
    }

    public static void createDb(Context ctx) {
        if (DATABASE == null) {
            Migration migration = new Migration(3, 4) {
                public void migrate(SupportSQLiteDatabase database) {
                    // if I have to write sql (and can't even reuse the db name const) what's the point of room?
                    database.execSQL("DROP TABLE `" + ChannelDao.CHANNEL_DB_NAME + "`");
                    database.execSQL("CREATE TABLE IF NOT EXISTS `ChatChannel` (`id` TEXT NOT NULL, `name` TEXT," +
                                     " `isPrivate` INTEGER NOT NULL, `lastMessage` TEXT, `last_message_time` INTEGER NOT NULL, PRIMARY KEY(`id`))");
                    // could I have just done "ALTER TABLE `ChatChannel` ALTER COLUMN `id` TEXT NOT NULL"? we'll never know
                }
            };
            DATABASE = Room.databaseBuilder(ctx, KolDB.class, "KolDB").addMigrations(migration).build();
        }
    }

    public abstract ChannelDao ChannelDao();

    public abstract MessageDao MessageDao();
}
