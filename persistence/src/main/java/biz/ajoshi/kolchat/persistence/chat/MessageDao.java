package biz.ajoshi.kolchat.persistence.chat;

import java.util.List;

import io.reactivex.Flowable;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by ajoshi on 7/14/17.
 */
@Dao
public interface MessageDao {
    @Query("SELECT * FROM chatmessage")
    List<ChatMessage> getMessages();

    // gets next pages. Offset might need to depend on hwo many items are shown (db could have more inserted in the meantime)
    @Query("SELECT * FROM (SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp DESC LIMIT 100 OFFSET :offset) ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForChannel (String channel_id, int offset);

    // gets 100 most recent commands, with the newest one at the bottom
    @Query("SELECT * FROM (SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp DESC LIMIT 100) ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForChannel (String channel_id);

    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp ASC LIMIT 1")
    Flowable<ChatMessage> getLastMessageForChannel(String channel_id);

    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp ASC LIMIT 1")
    LiveData<ChatMessage> getLastMessageLivedataForChannel(String channel_id);

    /*
     * We want to show all commands inserted into the db after a given time, but we also don't want to get the newest
     * right after it's been shown by the getMessagesForChannel call. So we ask for the newest that is newer than the
     * last message in that call
     * We do this by getting the newest message and looking for a

     */
    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id AND timeStamp=(SELECT timeStamp FROM chatmessage WHERE channelId = :channel_id AND timeStamp > :timestamp ORDER BY timeStamp DESC LIMIT 1)")
    LiveData<List<ChatMessage>> getLastMessagesLivedataForChannel2(String channel_id, long timestamp);

    /**
     * Get us the newest commands for the channel after the given local time. Also show System updates after that time
     * @param channel_id
     * @param localTimestamp
     * @return
     */
    @Query("SELECT * FROM chatmessage WHERE (channelId = :channel_id OR channelId = -1) AND localtimeStamp > :localTimestamp")
    LiveData<List<ChatMessage>> getLastMessagesLivedataForChannel(String channel_id, long localTimestamp);

    @Insert
    void insert(ChatMessage message);

    @Insert
    void insertAll(ChatMessage... message);

    @Delete
    void delete(ChatMessage message);
}
