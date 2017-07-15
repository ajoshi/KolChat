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
    @Query("SELECT * FROM chatchannel ORDER BY name DESC")
    Flowable<List<ChatChannel>> getAllChannels();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatChannel channel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLastMessage(ChatChannel channel);

    @Delete
    void delete(ChatChannel channel);
}
