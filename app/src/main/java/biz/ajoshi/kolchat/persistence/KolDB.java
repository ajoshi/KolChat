package biz.ajoshi.kolchat.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by ajoshi on 7/14/17.
 */

@Database(entities = { ChatMessage.class, ChatChannel.class }, version = 2)
public abstract class KolDB extends RoomDatabase {
    public abstract ChannelDao ChannelDao();

    public abstract MessageDao MessageDao();
}
