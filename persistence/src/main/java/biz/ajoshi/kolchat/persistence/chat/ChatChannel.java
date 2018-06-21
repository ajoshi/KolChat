package biz.ajoshi.kolchat.persistence.chat;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ajoshi on 7/14/17.
 */

@Entity
public class ChatChannel {
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public ChatChannel(@NonNull String id, boolean isPrivate, String name, String lastMessage, long lastMessageTime, String currentUserName, long lastTimeUserViewedChannel) {
        this.id = id;
        this.isPrivate = isPrivate;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.currentUserName = currentUserName;
        this.lastTimeUserViewedChannel = lastTimeUserViewedChannel;
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
    private String currentUserName;
    private long lastTimeUserViewedChannel;

    public long getLastTimeUserViewedChannel() {
        return lastTimeUserViewedChannel;
    }

    public void setLastTimeUserViewedChannel(long lastTimeUserViewedChannel) {
        this.lastTimeUserViewedChannel = lastTimeUserViewedChannel;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    private String lastMessage;
    @ColumnInfo(name = "last_message_time")
    private long lastMessageTime;

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }
}
