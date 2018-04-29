package biz.ajoshi.kolchat.persistence;

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
    @Query("SELECT * FROM chatchannel ORDER BY name ASC")
    Flowable<List<ChatChannel>> getAllChannels();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatChannel channel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLastMessage(ChatChannel channel);

    @Delete
    void delete(ChatChannel channel);
}
