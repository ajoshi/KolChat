package biz.ajoshi.kolchat.persistence;

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

    // gets 100 most recent messages, with the newest one at the bottom
    @Query("SELECT * FROM (SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp DESC LIMIT 100) ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForChannel (String channel_id);

    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp ASC LIMIT 1")
    Flowable<ChatMessage> getLastMessageForChannel(String channel_id);

    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp ASC LIMIT 1")
    LiveData<ChatMessage> getLastMessageLivedataForChannel(String channel_id);

    @Query("SELECT * FROM chatmessage WHERE channelId = :channel_id AND timeStamp=(SELECT timeStamp FROM chatmessage WHERE channelId = :channel_id ORDER BY timeStamp DESC LIMIT 1)")
    LiveData<List<ChatMessage>> getLastMessagesLivedataForChannel(String channel_id);

    @Insert
    void insert(ChatMessage message);

    @Insert
    void insertAll(ChatMessage... message);

    @Delete
    void delete(ChatMessage message);
}
