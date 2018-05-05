package biz.ajoshi.kolchat.persistence.chat;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String userName;
    private String text;
    private String channelId;
    private long timeStamp;
    private long localtimeStamp;
    private boolean shouldHideUsername;
    private String currentUserName;

    public ChatMessage(int id, String userId, String userName, String text, String channelId, long timeStamp,
            long localtimeStamp, boolean shouldHideUsername, String currentUserName) {

        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.channelId = channelId;
        this.timeStamp = timeStamp;
        this.localtimeStamp = localtimeStamp;
        this.shouldHideUsername = shouldHideUsername;
        this.currentUserName = currentUserName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    public long getLocaltimeStamp() {
        return localtimeStamp;
    }

    public void setLocaltimeStamp(long localtimeStamp) {
        this.localtimeStamp = localtimeStamp;
    }

    public boolean shouldHideUsername() {
        return shouldHideUsername;
    }

    public void setShouldHideUsername(boolean shouldHideUsername) {
        this.shouldHideUsername = shouldHideUsername;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }
}