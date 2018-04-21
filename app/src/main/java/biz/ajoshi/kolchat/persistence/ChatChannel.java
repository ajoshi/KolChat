package biz.ajoshi.kolchat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ajoshi on 7/14/17.
 */

@Entity
public class ChatChannel {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChatChannel(String id, boolean isPrivate, String name, String lastMessage, long lastMessageTime) {
        this.id = id;
        this.isPrivate = isPrivate;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private boolean isPrivate;

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    private String lastMessage;
    @ColumnInfo(name = "last_message_time")
    private long lastMessageTime;
}
