package biz.ajoshi.kolchat.persistence

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by ajoshi on 7/8/2017.
 */
@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel ORDER BY last_message_time DESC")
    fun getAllChannels () : Flowable<List <Channel>>

    @Insert
    fun insert(channel: Channel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateLastMessage(channel : Channel)

    @Delete
    fun delete(channel: Channel)
}
