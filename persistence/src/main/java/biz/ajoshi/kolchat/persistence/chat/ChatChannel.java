package biz.ajoshi.kolchat.persistence.chat;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Created by ajoshi on 7/14/17.
 */

@Entity(primaryKeys = {"id","currentUserId"})
public class ChatChannel {
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public ChatChannel(@NonNull String id, boolean isPrivate, String name, String lastMessage, long lastMessageTime, @NonNull String currentUserId, long lastTimeUserViewedChannel) {
        this.id = id;
        this.isPrivate = isPrivate;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.currentUserId = currentUserId;
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

    @NonNull
    private String id;
    @NonNull
    private String currentUserId;
    private String name;
    private boolean isPrivate;
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

    @NonNull
    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(@NonNull String currentUserId) {
        this.currentUserId = currentUserId;
    }
}
