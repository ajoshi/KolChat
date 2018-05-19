package biz.ajoshi.kolchat.persistence.chat;

import java.util.List;

import io.reactivex.Flowable;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface ChannelDao {
    // Name of the chat channel. not sure if I can use it in the @query one line below it
    String CHANNEL_DB_NAME = "ChatChannel";

    // Get groups ordered by name and ALSO the System chat
    @Query("SELECT * FROM chatchannel WHERE currentUserName=:userName AND (NOT isPrivate OR id == -1) ORDER BY name ASC")
    Flowable<List<ChatChannel>> getAllChatChannels(String userName);

    // Get PMs. We don't want System to show up here because it will almost always be up top, which is annoying
    @Query("SELECT * FROM chatchannel WHERE currentUserName=:userName AND isPrivate AND id != -1 ORDER BY last_message_time DESC")
    Flowable<List<ChatChannel>> getAllPMChannels(String userName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatChannel channel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLastMessage(ChatChannel channel);

    @Delete
    void delete(ChatChannel channel);
}
