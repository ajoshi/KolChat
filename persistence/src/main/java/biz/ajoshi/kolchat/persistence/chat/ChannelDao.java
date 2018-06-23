package biz.ajoshi.kolchat.persistence.chat;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface ChannelDao {
    // Name of the chat channel. not sure if I can use it in the @query one line below it
    String CHANNEL_DB_NAME = "chatchannel";

    // Get groups ordered by name and ALSO the System chat
    @Query("SELECT * FROM chatchannel WHERE currentUserName=:userName AND (NOT isPrivate OR id == -1) ORDER BY name ASC")
    Flowable<List<ChatChannel>> getAllChatChannels(String userName);

    // Get PMs. We don't want System to show up here because it will almost always be up top, which is annoying
    @Query("SELECT * FROM chatchannel WHERE currentUserName=:userName AND isPrivate AND id != -1 ORDER BY last_message_time DESC")
    Flowable<List<ChatChannel>> getAllPMChannels(String userName);

    // Get a single channel (and its data) based on its id
    @Query("SELECT * FROM chatchannel WHERE id=:id")
    Single<ChatChannel> getChannel(String id);

    // Update a channel's lasttimeviewed
    @Query("UPDATE chatchannel SET lastTimeUserViewedChannel = :newTime WHERE id=:id")
    void setChannelLastTime(String id, long newTime);

    // Forcibly insert a channel into the db
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatChannel channel);

    // Update a channel's last seen message and time
    @Query("UPDATE chatchannel SET lastMessage = :lastMessage, last_message_time = :lastMessageTime WHERE id=:id")
    void updateChannelWithNewMessage(String id, String lastMessage, long lastMessageTime);

    @Delete
    void delete(ChatChannel channel);
}
