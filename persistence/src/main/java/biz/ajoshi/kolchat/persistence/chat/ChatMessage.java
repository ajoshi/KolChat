package biz.ajoshi.kolchat.persistence.chat;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage implements Cloneable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    /**
     * Id of the user sending this message
     */
    private String userId;
    private String userName;
    private String text;
    /**
     * Id of the channel this was posted in
     */
    private String channelId;
    private long timeStamp;
    private long localtimeStamp;
    /**
     * True if the username should be hidden: Useful when displaying /me messages
     */
    private boolean shouldHideUsername;
    private String currentUserId;

    public ChatMessage(int id, String userId, String userName, String text, String channelId, long timeStamp,
                       long localtimeStamp, boolean shouldHideUsername, String currentUserId) {

        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.channelId = channelId;
        this.timeStamp = timeStamp;
        this.localtimeStamp = localtimeStamp;
        this.shouldHideUsername = shouldHideUsername;
        this.currentUserId = currentUserId;
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

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserID(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public ChatMessage clone() {
        return new ChatMessage(id, userId, userName, text, channelId, timeStamp, localtimeStamp, shouldHideUsername, currentUserId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
