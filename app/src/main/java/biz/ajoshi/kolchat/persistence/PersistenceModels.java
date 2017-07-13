package biz.ajoshi.kolchat.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by ajoshi on 7/9/2017.
 */

public class PersistenceModels {
    @Entity
    public static class Channel {
        @PrimaryKey
        public String id;
        public String name;
        public boolean isPrivate;
    }

    @Entity
    public static class Message {
        @PrimaryKey(autoGenerate = true)
        String userId;
        String userName;
        String text;
        String channelId;
        long timeStamp;
    }
}
